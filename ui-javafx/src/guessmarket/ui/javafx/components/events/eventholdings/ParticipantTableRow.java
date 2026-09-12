package guessmarket.ui.javafx.components.events.eventholdings;

import guessmarket.dto.OptionHoldingDto;
import guessmarket.dto.ParticipantHoldingsDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class ParticipantTableRow {
    private static final String NO_VALUE = "-";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;

    private final SimpleStringProperty userName;
    private final SimpleIntegerProperty firstOptionQuantity;
    private final SimpleStringProperty firstOptionValue;
    private final SimpleIntegerProperty secondOptionQuantity;
    private final SimpleStringProperty secondOptionValue;
    private final SimpleIntegerProperty openOrders;

    public ParticipantTableRow(ParticipantHoldingsDto participant) {
        List<OptionHoldingDto> holdings = participant.getHoldings();

        this.userName = new SimpleStringProperty(participant.getUserName());
        this.firstOptionQuantity = new SimpleIntegerProperty(quantityOf(holdings, FIRST_OPTION_INDEX));
        this.firstOptionValue = new SimpleStringProperty(valueOf(holdings, FIRST_OPTION_INDEX));
        this.secondOptionQuantity = new SimpleIntegerProperty(quantityOf(holdings, SECOND_OPTION_INDEX));
        this.secondOptionValue = new SimpleStringProperty(valueOf(holdings, SECOND_OPTION_INDEX));
        this.openOrders = new SimpleIntegerProperty(participant.getOpenOrders());
    }

    public String getUserName() {
        return userName.get();
    }

    public int getFirstOptionQuantity() {
        return firstOptionQuantity.get();
    }

    public String getFirstOptionValue() {
        return firstOptionValue.get();
    }

    public int getSecondOptionQuantity() {
        return secondOptionQuantity.get();
    }

    public String getSecondOptionValue() {
        return secondOptionValue.get();
    }

    public int getOpenOrders() {
        return openOrders.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public SimpleIntegerProperty firstOptionQuantityProperty() {
        return firstOptionQuantity;
    }

    public SimpleStringProperty firstOptionValueProperty() {
        return firstOptionValue;
    }

    public SimpleIntegerProperty secondOptionQuantityProperty() {
        return secondOptionQuantity;
    }

    public SimpleStringProperty secondOptionValueProperty() {
        return secondOptionValue;
    }

    public SimpleIntegerProperty openOrdersProperty() {
        return openOrders;
    }

    private static int quantityOf(List<OptionHoldingDto> holdings, int optionIndex) {
        return optionIndex < holdings.size() ? holdings.get(optionIndex).getQuantity() : 0;
    }

    // An option with no price yet has no value, which is different from being worth nothing.
    private static String valueOf(List<OptionHoldingDto> holdings, int optionIndex) {
        if (optionIndex >= holdings.size()) {
            return NO_VALUE;
        }
        return DisplayText.optionalAmount(holdings.get(optionIndex).getCurrentValue());
    }
}
