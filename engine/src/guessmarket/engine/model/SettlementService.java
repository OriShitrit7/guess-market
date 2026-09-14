package guessmarket.engine.model;

import guessmarket.engine.orderbook.MatchPlan;
import guessmarket.engine.orderbook.MintMatch;
import guessmarket.engine.orderbook.OrderMatch;

import java.util.List;

// Applies money and holding transfers.
public class SettlementService {
    private final UsersRegistry users;

    public SettlementService(UsersRegistry users) {
        this.users = users;
    }

    // Ensures that an order-only user is still recorded as an event participant.
    public void registerParticipation(MarketEvent event, String userName) {
        users.getUserByName(userName).joinEvent(event.getEventId());
    }

    // Moves the opening payment from the market maker and hands them their initial shares.
    public void settleOpening(MarketEvent event) {
        User marketMaker = users.getUserByName(event.getMarketMakerName());

        marketMaker.withdraw(event.calcOpeningCost()); // Charge the opening cost
        event.getAccount().deposit(event.calcOpeningCost()); // Fund the event account
        allocateInitialShares(event, marketMaker); // Record the initial holdings
    }

    // Transfers the cost of an LMSR purchase and records the buyer's shares and commission.
    public void settlePurchase(MarketEvent event, Trade trade) {
        User buyer = users.getUserByName(trade.getBuyerName());

        buyer.withdraw(trade.getSharesCost() + trade.getCommissionCost()); // Charge the buyer
        buyer.recordPurchase(event.getEventId(), trade); // Record shares and cost
        event.getAccount().deposit(trade.getSharesCost()); // Fund the event account
        payCommission(event, trade.getCommissionCost()); // Pay the market maker
    }

    // Applies every match and mint to its two sides, and returns the commission the submitting user paid.
    public double settleOrder(MarketEvent event, String submittingUser, MatchPlan plan) {
        double submittingUserCommission = 0;

        for (OrderMatch match : plan.getMatches()) { // Settle trades against sellers
            double commission = settleOrderMatch(event, match);

            if (match.getBuyerName().equals(submittingUser)) {
                submittingUserCommission += commission; // Count only the submitter's commission
            }
        }
        for (MintMatch mint : plan.getMints()) { // Settle newly created shares
            submittingUserCommission += settleMint(event, mint);
        }
        return submittingUserCommission; // Report the submitter's charge
    }

    // Pays every holder of the winning option, then reconciles the remaining balance with the market maker.
    public void settleClose(MarketEvent event, MarketOption winner) {
        users.getAllUsers().stream()
                .filter(user -> user.getQuantityInEvent(event.getEventId(), winner) > 0)
                .toList()
                .forEach(user -> payWinner(event, winner, user)); // Pay winning holders

        double remaining = event.getAccount().getBalance(); // Find funds left after payouts

        event.getAccount().payOut(remaining); // Empty the event account
        users.getUserByName(event.getMarketMakerName()).deposit(remaining); // Return the remainder
    }

    // Transfers shares and payment between the two users in a regular Order Book match.
    private double settleOrderMatch(MarketEvent event, OrderMatch match) {
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
        payCommission(event, commission); // Pay the market maker

        return commission;
    }

    // Both sides are buyers and no one sells: the shares are created and the money funds the event.
    private double settleMint(MarketEvent event, MintMatch mint) {
        settleMintSide(event, mint.getRestingBuyerName(), mint.getRestingOptionIndex(),
                mint.getQuantity(), mint.getRestingTotalPaid()); // Settle the waiting buyer

        return settleMintSide(event, mint.getIncomingBuyerName(), mint.getIncomingOptionIndex(),
                mint.getQuantity(), mint.getIncomingTotalPaid()); // Settle the incoming buyer
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
        payCommission(event, commission); // Pay the market maker

        return commission;
    }

    // Pays one winner after deducting any commission charged at close.
    private void payWinner(MarketEvent event, MarketOption winner, User user) {
        double payout = event.calcWinningPayout(user.getQuantityInEvent(event.getEventId(), winner));
        double commission = event.calcCloseCommission(payout);

        event.getAccount().payOut(payout); // Remove the gross payout
        user.deposit(payout - commission); // Pay the winner
        user.recordPayout(event.getEventId(), payout); // Record the gross amount
        user.recordCommission(event.getEventId(), commission);
        payCommission(event, commission); // Pay the market maker
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

    // Transfers a positive commission to the market maker and records it for the event.
    private void payCommission(MarketEvent event, double commission) {
        if (commission > 0) {
            users.getUserByName(event.getMarketMakerName()).deposit(commission);
            event.getAccount().collectCommission(commission);
        }
    }
}
