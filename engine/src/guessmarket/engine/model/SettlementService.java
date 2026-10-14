package guessmarket.engine.model;

import guessmarket.engine.orderbook.MatchPlan;
import guessmarket.engine.orderbook.MintMatch;
import guessmarket.engine.orderbook.OrderMatch;

import java.util.List;

// Applies the money and share transfers of every market operation to the users and the event account,
// and records them in the users' account history.
public class SettlementService {
    private final UsersRegistry users;

    public SettlementService(UsersRegistry users) {
        this.users = users;
    }

    // Ensures that an order-only user is still recorded as an event participant.
    public void registerParticipation(MarketEvent event, String userName) {
        users.getUserByName(userName).joinEvent(event.getEventId());
    }

    // Adds loaded funds to the user's balance, which may release a blocked user.
    public void settleLoadFunds(String userName, double amount) {
        User user = users.getUserByName(userName);

        user.deposit(amount);
        user.releaseBlockIfSolvent();
        user.recordTransaction(AccountTransactionType.LOAD_FUNDS, amount);
    }

    // Moves the opening payment from the market maker and hands them their initial shares.
    public void settleOpening(MarketEvent event) {
        User marketMaker = users.getUserByName(event.getMarketMakerName());
        double openingCost = event.calcOpeningCost();

        marketMaker.withdraw(openingCost); // Charge the opening cost
        event.getAccount().deposit(openingCost); // Fund the event account
        marketMaker.recordTransaction(AccountTransactionType.EVENT_OPENING, -openingCost);
        allocateInitialShares(event, marketMaker); // Record the initial holdings
    }

    // Transfers the cost of an LMSR purchase and records the buyer's shares and commission.
    public void settlePurchase(MarketEvent event, Trade trade) {
        User buyer = users.getUserByName(trade.getBuyerName());
        double totalCost = trade.getSharesCost() + trade.getCommissionCost();

        buyer.withdraw(totalCost); // Charge the buyer
        buyer.recordTransaction(AccountTransactionType.SHARES_PURCHASE, -totalCost);
        buyer.recordPurchase(event.getEventId(), trade); // Record shares and cost
        event.getAccount().deposit(trade.getSharesCost()); // Fund the event account
        payPurchaseCommission(event, trade.getCommissionCost()); // Pay the market maker
    }

    // Applies every match and mint to its two sides, and returns the commission the submitting user paid.
    public double settleOrder(MarketEvent event, String submittingUser, MatchPlan plan) {
        User submitter = users.getUserByName(submittingUser);
        double balanceBefore = submitter.getBalance();
        double submittingUserCommission = 0;
        double totalCommission = 0;

        for (OrderMatch match : plan.getMatches()) { // Settle trades against sellers
            double commission = settleOrderMatch(event, match, submittingUser);
            totalCommission += commission;

            if (match.getBuyerName().equals(submittingUser)) {
                submittingUserCommission += commission; // Count only the submitter's commission
            }
        }
        for (MintMatch mint : plan.getMints()) { // Settle newly created shares
            double restingCommission = settleRestingMintSide(event, mint);
            double incomingCommission = settleMintSide(event, mint.getIncomingBuyerName(), mint.getIncomingOptionIndex(), mint.getQuantity(), mint.getIncomingTotalPaid());

            submittingUserCommission += incomingCommission;
            totalCommission += restingCommission + incomingCommission;
        }

        // The submitter gets one account line for the whole order, and the market maker one for its
        // commission, so that every recorded balance matches the balance after the order.
        double submitterChange = submitter.getBalance() - balanceBefore;
        submitter.recordTransaction(submitterChange < 0 ? AccountTransactionType.SHARES_PURCHASE : AccountTransactionType.SHARES_SALE, submitterChange);
        payPurchaseCommission(event, totalCommission);
        return submittingUserCommission; // Report the submitter's charge
    }

    // Pays every holder of the winning option, then reconciles the remaining balance with the market maker.
    public void settleClose(MarketEvent event, MarketOption winner) {
        List<User> winners = users.getAllUsers().stream()
                .filter(user -> user.getQuantityInEvent(event.getEventId(), winner) > 0)
                .toList();
        double closeCommission = winners.stream()
                .mapToDouble(user -> payWinner(event, winner, user))
                .sum(); // Pay winning holders

        double remaining = event.getAccount().getBalance(); // Find funds left after payouts
        User marketMaker = users.getUserByName(event.getMarketMakerName());

        event.getAccount().payOut(remaining); // Empty the event account
        payCommission(event, closeCommission);
        marketMaker.deposit(remaining); // Return the remainder
        marketMaker.recordTransaction(AccountTransactionType.EVENT_CLOSING, closeCommission + remaining);
    }

    // Transfers shares and payment between the two users in a regular Order Book match.
    private double settleOrderMatch(MarketEvent event, OrderMatch match, String submittingUser) {
        User buyer = users.getUserByName(match.getBuyerName());
        User seller = users.getUserByName(match.getSellerName());
        MarketOption option = event.getOptions().get(match.getOptionIndex());

        double amount = match.getTotalPaid();
        double commission = event.calcPurchaseCommission(amount);

        seller.removeShares(event.getEventId(), option, match.getQuantity()); // Transfer the shares
        buyer.addShares(event.getEventId(), option, match.getQuantity(), amount);

        buyer.withdraw(amount + commission); // Charge the buyer
        seller.deposit(amount); // Pay the seller
        seller.recordSale(event.getEventId(), amount); // Record sale proceeds
        buyer.recordCommission(event.getEventId(), commission); // Record buyer commission

        // Only the other side is recorded here. The submitter's line is written once, after the whole order.
        if (buyer.getName().equals(submittingUser)) {
            seller.recordTransaction(AccountTransactionType.SHARES_SALE, amount);
        } else {
            buyer.recordTransaction(AccountTransactionType.SHARES_PURCHASE, -(amount + commission));
        }
        return commission;
    }

    // Settles the waiting side of a mint and records the purchase in that user's account history.
    private double settleRestingMintSide(MarketEvent event, MintMatch mint) {
        double commission = settleMintSide(event, mint.getRestingBuyerName(), mint.getRestingOptionIndex(), mint.getQuantity(), mint.getRestingTotalPaid());

        users.getUserByName(mint.getRestingBuyerName())
                .recordTransaction(AccountTransactionType.SHARES_PURCHASE, -(mint.getRestingTotalPaid() + commission));
        return commission;
    }

    // Charges one side of a mint and records the newly created shares.
    private double settleMintSide(MarketEvent event, String buyerName, int optionIndex, int quantity, double amount) {
        User buyer = users.getUserByName(buyerName);
        MarketOption option = event.getOptions().get(optionIndex);
        double commission = event.calcPurchaseCommission(amount);

        buyer.addShares(event.getEventId(), option, quantity, amount); // Record the new shares
        buyer.withdraw(amount + commission); // Charge the buyer
        buyer.recordCommission(event.getEventId(), commission);
        event.getAccount().deposit(amount); // Fund the event account

        return commission;
    }

    // Pays one winner after deducting any commission charged at close.
    private double payWinner(MarketEvent event, MarketOption winner, User user) {
        double payout = event.calcWinningPayout(user.getQuantityInEvent(event.getEventId(), winner));
        double commission = event.calcCloseCommission(payout);

        event.getAccount().payOut(payout); // Remove the gross payout
        user.deposit(payout - commission); // Pay the winner
        user.recordTransaction(AccountTransactionType.WINNING_PAYOUT, payout - commission);
        user.recordPayout(event.getEventId(), payout); // Record the gross amount
        user.recordCommission(event.getEventId(), commission);

        return commission;
    }

    // Adds the opening shares and their cost to the market maker's position.
    private void allocateInitialShares(MarketEvent event, User marketMaker) {
        int quantity = event.calcInitialSharesPerOption();

        if (quantity == 0) {
            return;
        }
        double amountPaidPerOption = event.calcInitialAmountPerOption();

        event.getOptions().forEach(option ->
                marketMaker.addShares(event.getEventId(), option, quantity, amountPaidPerOption));
    }

    // Pays the commission and adds it to the market maker's account history.
    private void payPurchaseCommission(MarketEvent event, double commission) {
        payCommission(event, commission);
        users.getUserByName(event.getMarketMakerName()).recordTransaction(AccountTransactionType.COMMISSION_RECEIVED, commission);
    }

    // Transfers a positive commission to the market maker and records it for the event.
    private void payCommission(MarketEvent event, double commission) {
        if (commission > 0) {
            users.getUserByName(event.getMarketMakerName()).deposit(commission);
            event.getAccount().collectCommission(commission);
        }
    }
}
