package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
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

    private List<GMEvent> extractEvents(GuessMarket xmlRoot) throws InvalidFileException {
        GMEvents eventsElement = xmlRoot.getGMEvents();

        if (eventsElement == null || eventsElement.getGMEvent().isEmpty()) {
            throw new InvalidFileException(FileErrorCode.NO_EVENTS);
        }
        return eventsElement.getGMEvent();
    }

    // Builds one complete market event after checking its required XML data.
    private MarketEvent mapEvent(GMEvent source, int eventNumber) throws InvalidFileException {
        String eventName = mapEventName(source, eventNumber);

        // JAXB may use zero when a primitive integer value is missing or invalid.
        if (source.getId() <= 0) {
            throw new InvalidFileException(FileErrorCode.INVALID_EVENT_ID, eventNumber);
        }

        List<MarketOption> options = mapOptions(source, eventName);

        return new MarketEvent(source.getId(), eventName,
                requireValue(source.getDescription(), eventName, DESCRIPTION_ELEMENT),
                options, mapCommission(source, eventName, eventNumber),
                mapTradingMethod(source, eventName, options));
    }

    private String mapEventName(GMEvent source, int eventNumber) throws InvalidFileException {
        String eventName = source.getName();

        if (eventName == null || eventName.isBlank()) {
            throw new InvalidFileException(FileErrorCode.MISSING_ATTRIBUTE, EVENT_ENTITY, eventNumber,
                    EVENT_ELEMENT, NAME_ATTRIBUTE);
        }
        return eventName.trim();
    }

    // Maps the XML commission value and policy to the engine's commission configuration.
    private CommissionConfig mapCommission(GMEvent source, String eventName, int eventNumber)
            throws InvalidFileException {
        Commission commissionElement = source.getCommission();

        if (commissionElement == null) {
            throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, COMMISSION_ELEMENT);
        }
        if (commissionElement.getType() == null) {
            throw new InvalidFileException(FileErrorCode.MISSING_ATTRIBUTE, EVENT_ENTITY, eventNumber,
                    COMMISSION_ELEMENT, TYPE_ATTRIBUTE);
        }

        String policyValue = commissionElement.getType().trim();

        CommissionPolicy policy = switch (policyValue) {
            case ON_PURCHASE_VALUE -> CommissionPolicy.ON_PURCHASE;
            case ON_CLOSE_VALUE -> CommissionPolicy.ON_CLOSE;
            default -> throw new InvalidFileException(FileErrorCode.UNKNOWN_COMMISSION_TYPE, eventName, policyValue);
        };

        return new CommissionConfig(commissionElement.getValue(), policy);
    }

    private List<MarketOption> mapOptions(GMEvent source, String eventName) throws InvalidFileException {
        GMOptions optionsElement = source.getGMOptions();

        if (optionsElement == null) {
            throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, OPTIONS_ELEMENT);
        }

        List<MarketOption> options = new ArrayList<>();

        for (String rawOptionName : optionsElement.getGMOption()) {
            options.add(new MarketOption(requireValue(rawOptionName, eventName, OPTION_ELEMENT)));
        }
        return options;
    }

    // Creates the configured trading method and reports an invalid liquidity value as a file error.
    private TradingMethod mapTradingMethod(GMEvent source, String eventName,
                                           List<MarketOption> options) throws InvalidFileException {
        GMMethod methodElement = source.getGMMethod();

        if (methodElement == null) {
            throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, METHOD_ELEMENT);
        }

        GMLMSR lmsrElement = methodElement.getGMLMSR();

        if (lmsrElement != null) {
            return mapLmsrMethod(lmsrElement, eventName);
        }

        GMOrderBook orderBookElement = methodElement.getGMOrderBook();

        if (orderBookElement != null) {
            return mapOrderBookMethod(orderBookElement, options);
        }
        throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, TRADING_METHOD_ELEMENTS);
    }

    private TradingMethod mapLmsrMethod(GMLMSR lmsrElement, String eventName) throws InvalidFileException {
        try {
            return new LmsrTradingMethod(lmsrElement.getB());
        } catch (IllegalArgumentException e) {
            throw new InvalidFileException(FileErrorCode.INVALID_LIQUIDITY, e, eventName, lmsrElement.getB());
        }
    }

    private TradingMethod mapOrderBookMethod(GMOrderBook orderBookElement, List<MarketOption> options) {
        return new OrderBookTradingMethod(orderBookElement.getD(),
                Boolean.parseBoolean(orderBookElement.getAllowMint()), orderBookElement.getInitial(), options);
    }

    // Rejects an element that is absent from the file or carries no readable text.
    private String requireValue(String rawText, String eventName, String elementName) throws InvalidFileException {
        String text = rawText == null ? "" : rawText.trim();

        if (text.isEmpty()) {
            throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, elementName);
        }
        return text;
    }
}
