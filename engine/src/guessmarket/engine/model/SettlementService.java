package guessmarket.engine.model;

import guessmarket.engine.orderbook.MatchPlan;
import guessmarket.engine.orderbook.MintMatch;
import guessmarket.engine.orderbook.OrderMatch;

import java.util.List;

// Applies money and holding transfers. Pricing, matching and validation belong elsewhere.
public class SettlementService {
    private final UsersRegistry users;

    public SettlementService(UsersRegistry users) {
        this.users = users;
    }

    public void registerParticipation(MarketEvent event, String userName) {
        users.getUserByName(userName).joinEvent(event.getEventId());
    }

    // Moves the opening payment from the market maker and hands them their initial shares.
    public void settleOpening(MarketEvent event) {
        User marketMaker = users.getUserByName(event.getMarketMakerName());

        marketMaker.withdraw(event.calcOpeningCost());
        event.getAccount().deposit(event.calcOpeningCost());
        allocateInitialShares(event, marketMaker);
    }

    public void settlePurchase(MarketEvent event, Trade trade) {
        User buyer = users.getUserByName(trade.getBuyerName());

        buyer.withdraw(trade.getSharesCost() + trade.getCommissionCost());
        buyer.recordPurchase(event.getEventId(), trade);
        event.getAccount().deposit(trade.getSharesCost());
        payCommission(event, trade.getCommissionCost());
    }

    // Applies every match and mint to its two sides, and returns the commission the submitting user paid.
    public double settleOrder(MarketEvent event, String submittingUser, MatchPlan plan) {
        double submittingUserCommission = 0;

        for (OrderMatch match : plan.getMatches()) {
            double commission = settleOrderMatch(event, match);

            if (match.getBuyerName().equals(submittingUser)) {
                submittingUserCommission += commission;
            }
        }
        for (MintMatch mint : plan.getMints()) {
            submittingUserCommission += settleMint(event, mint);
        }
        return submittingUserCommission;
    }

    // Pays every holder of the winning option, then reconciles the remaining balance with the market maker.
    public void settleClose(MarketEvent event, MarketOption winner) {
        users.getAllUsers().stream()
                .filter(user -> user.getQuantityInEvent(event.getEventId(), winner) > 0)
                .toList()
                .forEach(user -> payWinner(event, winner, user));

        double remaining = event.getAccount().getBalance();

        event.getAccount().payOut(remaining);
        users.getUserByName(event.getMarketMakerName()).deposit(remaining);
    }

    private double settleOrderMatch(MarketEvent event, OrderMatch match) {
        User buyer = users.getUserByName(match.getBuyerName());
        User seller = users.getUserByName(match.getSellerName());
        MarketOption option = event.getOptions().get(match.getOptionIndex());

        double amount = match.getTotalPaid();
        double commission = event.calcPurchaseCommission(amount);

        seller.removeShares(event.getEventId(), option, match.getQuantity());
        buyer.addShares(event.getEventId(), option, match.getQuantity(), amount);

        buyer.withdraw(amount + commission);
        seller.deposit(amount);
        seller.recordSale(event.getEventId(), amount);
        buyer.recordCommission(event.getEventId(), commission);
        payCommission(event, commission);

        return commission;
    }

    // Both sides are buyers and no one sells: the shares are created and the money funds the event.
    private double settleMint(MarketEvent event, MintMatch mint) {
        settleMintSide(event, mint.getRestingBuyerName(), mint.getRestingOptionIndex(),
                mint.getQuantity(), mint.getRestingTotalPaid());

        return settleMintSide(event, mint.getIncomingBuyerName(), mint.getIncomingOptionIndex(),
                mint.getQuantity(), mint.getIncomingTotalPaid());
    }

    private double settleMintSide(MarketEvent event, String buyerName, int optionIndex, int quantity, double amount) {
        User buyer = users.getUserByName(buyerName);
        MarketOption option = event.getOptions().get(optionIndex);
        double commission = event.calcPurchaseCommission(amount);

        buyer.addShares(event.getEventId(), option, quantity, amount);
        buyer.withdraw(amount + commission);
        buyer.recordCommission(event.getEventId(), commission);
        event.getAccount().deposit(amount);
        payCommission(event, commission);

        return commission;
    }

    private void payWinner(MarketEvent event, MarketOption winner, User user) {
        double payout = event.calcWinningPayout(user.getQuantityInEvent(event.getEventId(), winner));
        double commission = event.calcCloseCommission(payout);

        event.getAccount().payOut(payout);
        user.deposit(payout - commission);
        user.recordPayout(event.getEventId(), payout);
        user.recordCommission(event.getEventId(), commission);
        payCommission(event, commission);
    }

    private void allocateInitialShares(MarketEvent event, User marketMaker) {
        int quantity = event.calcInitialSharesPerOption();

        if (quantity == 0) {
            return;
        }
        double amountPaidPerOption = event.calcInitialAmountPerOption();

        event.getOptions().forEach(option ->
                marketMaker.addShares(event.getEventId(), option, quantity, amountPaidPerOption));
    }

    private void payCommission(MarketEvent event, double commission) {
        if (commission > 0) {
            users.getUserByName(event.getMarketMakerName()).deposit(commission);
            event.getAccount().collectCommission(commission);
        }
    }
}
