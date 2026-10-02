package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.model.LmsrMarketEvent;
import guessmarket.engine.model.OrderBookMarketEvent;
import guessmarket.engine.xml.generated.*;

import java.util.ArrayList;
import java.util.List;

// Converts JAXB event data into model events and reports missing or unsupported XML values.
public class EventXmlMapper {
    private static final String EVENT_ENTITY = "Event";
    private static final String EVENT_ELEMENT = "GM-event";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String COMMISSION_ELEMENT = "commission";
    private static final String OPTIONS_ELEMENT = "GM-options";
    private static final String OPTION_ELEMENT = "GM-option";
    private static final String METHOD_ELEMENT = "GM-method";
    private static final String TRADING_METHOD_ELEMENTS = "GM-LMSR> or <GM-order-book";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";

    private static final String ON_PURCHASE_VALUE = "on-purchase";
    private static final String ON_CLOSE_VALUE = "on-close";

    // Maps every XML event to a model event while preserving its position in the source file.
    public List<MarketEvent> mapEvents(GuessMarket xmlRoot, int nextEventId) throws InvalidFileException {
        List<MarketEvent> mappedEvents = new ArrayList<>();
        List<GMEvent> xmlEvents = extractEvents(xmlRoot); // Require at least one event

        for (int i = 0; i < xmlEvents.size(); i++) {
            mappedEvents.add(mapEvent(xmlEvents.get(i), i + 1, nextEventId + i)); // Preserve the XML position
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
    private MarketEvent mapEvent(GMEvent source, int eventNumber, int eventId) throws InvalidFileException {
        String eventName = mapEventName(source, eventNumber); // Validate the event name
        List<MarketOption> options = mapOptions(source, eventName); // Map the event options

        String description = requireValue(source.getDescription(), eventName, DESCRIPTION_ELEMENT);
        CommissionConfig commission = mapCommission(source, eventName, eventNumber); // Map commission settings
        return mapMarketEvent(source, eventId, eventName, description, options, commission); // Build the event subtype
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

    // Chooses the event subtype at the XML boundary; no separate TradingMethod object is created.
    private MarketEvent mapMarketEvent(GMEvent source, int eventId, String eventName, String description,
                                        List<MarketOption> options, CommissionConfig commission) throws InvalidFileException {
        GMMethod methodElement = source.getGMMethod(); // Read the selected trading method
        if (methodElement == null) {
            throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, METHOD_ELEMENT);
        }

        GMLMSR lmsrElement = methodElement.getGMLMSR();
        if (lmsrElement != null) {
            try {
                return new LmsrMarketEvent(eventId, eventName, description, options, commission, lmsrElement.getB());
            } catch (IllegalArgumentException e) {
                throw new InvalidFileException(FileErrorCode.INVALID_LIQUIDITY, e, eventName, lmsrElement.getB());
            }
        }

        GMOrderBook orderBook = methodElement.getGMOrderBook();
        if (orderBook != null) {
            try {
                return new OrderBookMarketEvent(eventId, eventName, description, options, commission,
                        orderBook.getD(), Boolean.parseBoolean(orderBook.getAllowMint()), orderBook.getInitial());
            } catch (IllegalArgumentException e) {
                throw new InvalidFileException(FileErrorCode.INVALID_ORDER_BOOK_CONFIG, e,
                        eventName, orderBook.getD(), orderBook.getInitial());
            }
        }
        throw new InvalidFileException(FileErrorCode.MISSING_ELEMENT, eventName, TRADING_METHOD_ELEMENTS);
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
