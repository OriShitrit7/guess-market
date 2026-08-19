package guessmarket.engine.xml;

import guessmarket.engine.exception.*;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.trading.LmsrTradingMethod;
import guessmarket.engine.trading.TradingMethod;
import guessmarket.engine.xml.generated.Comision;
import guessmarket.engine.xml.generated.GMEvent;
import guessmarket.engine.xml.generated.GMEvents;
import guessmarket.engine.xml.generated.GMLMSR;
import guessmarket.engine.xml.generated.GMMethod;
import guessmarket.engine.xml.generated.GMOptions;
import guessmarket.engine.xml.generated.GuessMarket;

import java.util.ArrayList;
import java.util.List;

public class EventXmlMapper {
    public static final String EVENT_ELEMENT = "GM-event";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String COMMISSION_ELEMENT = "comision";
    private static final String OPTIONS_ELEMENT = "GM-options";
    private static final String METHOD_ELEMENT = "GM-method";
    private static final String LMSR_ELEMENT = "GM-LMSR";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";

    private static final String ON_PURCHASE_VALUE = "on-purchase";
    private static final String ON_CLOSE_VALUE = "on-close";

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

    private MarketEvent mapEvent(GMEvent source, int eventPosition) throws InvalidFileException {
        String eventName = mapEventName(source, eventPosition);

        if (source.getId() <= 0) {
            throw new InvalidEventIdException(eventPosition);
        }
        if (source.getDescription() == null) {
            throw new MissingElementException(eventName, DESCRIPTION_ELEMENT);
        }

        return new MarketEvent(source.getId(), eventName, source.getDescription().trim(),
                mapOptions(source, eventName), mapCommission(source, eventName, eventPosition),
                mapTradingMethod(source, eventName));
    }

    private String mapEventName(GMEvent source, int eventPosition) throws MissingAttributeException {
        List<String> nameTokens = source.getName();

        if (nameTokens == null || nameTokens.isEmpty()) {
            throw new MissingAttributeException(eventPosition, EVENT_ELEMENT, NAME_ATTRIBUTE);
        }
        return String.join(" ", nameTokens);
    }

    private CommissionConfig mapCommission(GMEvent source, String eventName, int eventPosition)
            throws InvalidFileException {
        Comision commissionElement = source.getComision();

        if (commissionElement == null) {
            throw new MissingElementException(eventName, COMMISSION_ELEMENT);
        }
        if (commissionElement.getType() == null) {
            throw new MissingAttributeException(eventPosition, COMMISSION_ELEMENT, TYPE_ATTRIBUTE);
        }

        String policyValue = commissionElement.getType().trim();

        CommissionPolicy policy = switch (policyValue) {
            case ON_PURCHASE_VALUE -> CommissionPolicy.ON_PURCHASE;
            case ON_CLOSE_VALUE -> CommissionPolicy.ON_CLOSE;
            default -> throw new UnknownCommissionTypeException(eventName, policyValue);
        };

        return new CommissionConfig(commissionElement.getValue(), policy);
    }

    private List<MarketOption> mapOptions(GMEvent source, String eventName) throws MissingElementException {
        GMOptions optionsElement = source.getGMOptions();

        if (optionsElement == null) {
            throw new MissingElementException(eventName, OPTIONS_ELEMENT);
        }

        return optionsElement.getGMOption().stream()
                .map(String::trim)
                .map(MarketOption::new)
                .toList();
    }

    private TradingMethod mapTradingMethod(GMEvent source, String eventName) throws InvalidFileException {
        GMMethod methodElement = source.getGMMethod();

        if (methodElement == null) {
            throw new MissingElementException(eventName, METHOD_ELEMENT);
        }

        GMLMSR lmsrElement = methodElement.getGMLMSR();

        if (lmsrElement == null) {
            throw new MissingElementException(eventName, LMSR_ELEMENT);
        }

        try {
            return new LmsrTradingMethod(lmsrElement.getB());
        } catch (IllegalArgumentException e) {
            throw new InvalidLiquidityException(eventName, lmsrElement.getB());
        }
    }
}
