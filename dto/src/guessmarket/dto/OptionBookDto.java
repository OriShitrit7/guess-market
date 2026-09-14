package guessmarket.dto;

import java.util.List;

// Carries one option's waiting orders and price statistics for the Order Book views.
// A statistic is null while the order or trade data needed to calculate it is unavailable.
public final class OptionBookDto {
    private final String optionName;
    private final List<OrderEntryDto> buyOrders;
    private final List<OrderEntryDto> sellOrders;
    private final Double lastTradePrice;
    private final Double bid;
    private final Double ask;
    private final Double mid;
    private final Double spread;

    public OptionBookDto(String optionName, List<OrderEntryDto> buyOrders, List<OrderEntryDto> sellOrders,
                         Double lastTradePrice, Double bid, Double ask, Double mid, Double spread) {
        this.optionName = optionName;
        this.buyOrders = List.copyOf(buyOrders);
        this.sellOrders = List.copyOf(sellOrders);
        this.lastTradePrice = lastTradePrice;
        this.bid = bid;
        this.ask = ask;
        this.mid = mid;
        this.spread = spread;
    }

    public String getOptionName() {
        return optionName;
    }

    public List<OrderEntryDto> getBuyOrders() {
        return buyOrders;
    }

    public List<OrderEntryDto> getSellOrders() {
        return sellOrders;
    }

    public Double getLastTradePrice() {
        return lastTradePrice;
    }

    public Double getBid() {
        return bid;
    }

    public Double getAsk() {
        return ask;
    }

    public Double getMid() {
        return mid;
    }

    public Double getSpread() {
        return spread;
    }
}
