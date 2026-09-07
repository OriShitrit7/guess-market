package guessmarket.engine.xml;

import guessmarket.engine.exception.file.InvalidEventIdException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.file.InvalidLiquidityException;
import guessmarket.engine.exception.file.MissingAttributeException;
import guessmarket.engine.exception.file.MissingElementException;
import guessmarket.engine.exception.file.NoEventsException;
import guessmarket.engine.exception.file.UnknownCommissionTypeException;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.trading.LmsrTradingMethod;
import guessmarket.engine.trading.OrderBookTradingMethod;
import guessmarket.engine.trading.TradingMethod;
import guessmarket.engine.xml.generated.*;

import java.util.ArrayList;
import java.util.List;

// Converts JAXB-generated XML objects into the market engine's model objects.
// Reports missing,empty or unsupported XML data through detailed file exceptions.
public class EventXmlMapper {
    private static final String EVENT_ENTITY = "Event";
    private static final String EVENT_ELEMENT = "GM-event";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String COMMISSION_ELEMENT = "commission";
    private static final String OPTIONS_ELEMENT = "GM-options";
    private static final String OPTION_ELEMENT = "GM-option";
    private static final String METHOD_ELEMENT = "GM-method";
    private static final String LMSR_ELEMENT = "GM-LMSR";
    private static final String TRADING_METHOD_ELEMENTS = "GM-LMSR> or <GM-order-book";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";

    private static final String ON_PURCHASE_VALUE = "on-purchase";
    private static final String ON_CLOSE_VALUE = "on-close";

    // Maps every XML event to a model event while preserving its position in the source file.
    public List<MarketEvent> mapEvents(GuessMarket xmlRoot) throws InvalidFileException {
        List<MarketEvent> mappedEvents = new ArrayList<>();
        List<GMEvent> xmlEvents = extractEvents(xmlRoot);

        for (int i = 0; i < xmlEvents.size(); i++) {
            mappedEvents.add(mapEvent(xmlEvents.get(i), i + 1));
        }
        return mappedEvents;
    }

    private List<GMEvent> extractEvents(GuessMarket xmlRoot) throws NoEventsException {
        GMEvents eventsElement = xmlRoot.getGMEvents();

        if (eventsElement == null || eventsElement.getGMEvent().isEmpty()) {
            throw new NoEventsException();
        }
        return eventsElement.getGMEvent();
    }

    // Builds one complete market event after checking its required XML data.
    private MarketEvent mapEvent(GMEvent source, int eventNumber) throws InvalidFileException {
        String eventName = mapEventName(source, eventNumber);

        // JAXB may use zero when a primitive integer value is missing or invalid.
        if (source.getId() <= 0) {
            throw new InvalidEventIdException(eventNumber);
        }

        return new MarketEvent(source.getId(), eventName,
                requireValue(source.getDescription(), eventName, DESCRIPTION_ELEMENT),
                mapOptions(source, eventName), mapCommission(source, eventName, eventNumber),
                mapTradingMethod(source, eventName));
    }

    private String mapEventName(GMEvent source, int eventNumber) throws MissingAttributeException {
        String eventName = source.getName();

        if (eventName == null || eventName.isBlank()) {
            throw new MissingAttributeException(EVENT_ENTITY, eventNumber, EVENT_ELEMENT, NAME_ATTRIBUTE);
        }
        return eventName.trim();
    }

    // Maps the XML commission value and policy to the engine's commission configuration.
    private CommissionConfig mapCommission(GMEvent source, String eventName, int eventNumber)
            throws InvalidFileException {
        Commission commissionElement = source.getCommission();

        if (commissionElement == null) {
            throw new MissingElementException(eventName, COMMISSION_ELEMENT);
        }
        if (commissionElement.getType() == null) {
            throw new MissingAttributeException(EVENT_ENTITY, eventNumber, COMMISSION_ELEMENT,
                    TYPE_ATTRIBUTE);
        }

        String policyValue = commissionElement.getType().trim();

        CommissionPolicy policy = switch (policyValue) {
            case ON_PURCHASE_VALUE -> CommissionPolicy.ON_PURCHASE;
            case ON_CLOSE_VALUE -> CommissionPolicy.ON_CLOSE;
            default -> throw new UnknownCommissionTypeException(eventName, policyValue);
        };

        return new CommissionConfig(commissionElement.getValue(), policy);
    }

    private List<MarketOption> mapOptions(GMEvent source, String eventName) throws InvalidFileException {
        GMOptions optionsElement = source.getGMOptions();

        if (optionsElement == null) {
            throw new MissingElementException(eventName, OPTIONS_ELEMENT);
        }

        List<MarketOption> options = new ArrayList<>();

        for (String rawOptionName : optionsElement.getGMOption()) {
            options.add(new MarketOption(requireValue(rawOptionName, eventName, OPTION_ELEMENT)));
        }
        return options;
    }

    // Creates the configured trading method and reports an invalid liquidity value as a file error.
    private TradingMethod mapTradingMethod(GMEvent source, String eventName) throws InvalidFileException {
        GMMethod methodElement = source.getGMMethod();

        if (methodElement == null) {
            throw new MissingElementException(eventName, METHOD_ELEMENT);
        }

        GMLMSR lmsrElement = methodElement.getGMLMSR();

        if (lmsrElement != null) {
            return mapLmsrMethod(lmsrElement, eventName);
        }

        GMOrderBook orderBookElement = methodElement.getGMOrderBook();

        if (orderBookElement != null) {
            return mapOrderBookMethod(orderBookElement);
        }
        throw new MissingElementException(eventName, TRADING_METHOD_ELEMENTS);
    }

    private TradingMethod mapLmsrMethod(GMLMSR lmsrElement, String eventName) throws InvalidLiquidityException {
        try {
            return new LmsrTradingMethod(lmsrElement.getB());
        } catch (IllegalArgumentException e) {
            throw new InvalidLiquidityException(eventName, lmsrElement.getB());
        }
    }

    private TradingMethod mapOrderBookMethod(GMOrderBook orderBookElement) {
        return new OrderBookTradingMethod(orderBookElement.getD(),
                Boolean.parseBoolean(orderBookElement.getAllowMint()), orderBookElement.getInitial());
    }

    // Rejects an element that is absent from the file or carries no readable text.
    private String requireValue(String rawText, String eventName, String elementName) throws MissingElementException {
        String text = rawText == null ? "" : rawText.trim();

        if (text.isEmpty()) {
            throw new MissingElementException(eventName, elementName);
        }
        return text;
    }
}
