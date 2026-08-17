package guessmarket.engine.xml;

import guessmarket.engine.exception.*;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.EventAccount;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.trading.LmsrTradingMethod;
import guessmarket.engine.trading.TradingMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class EventXmlMapper {
    private static final String EVENT_ELEMENT = "GM-event";
    private static final String ID_ELEMENT = "id";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String COMMISSION_ELEMENT = "comision";
    private static final String OPTION_ELEMENT = "GM-option";
    private static final String METHOD_ELEMENT = "GM-method";
    private static final String LMSR_ELEMENT = "GM-LMSR";
    private static final String B_ELEMENT = "b";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";

    private static final String ON_PURCHASE_VALUE = "on-purchase";
    private static final String ON_CLOSE_VALUE = "on-close";

    public List<MarketEvent> mapEvents(Document document) throws InvalidFileException {
        List<MarketEvent> mappedEvents = new ArrayList<>();
        NodeList eventNodes = document.getElementsByTagName(EVENT_ELEMENT);

        for (int i = 0; i < eventNodes.getLength(); i++) {
            mappedEvents.add(mapEvent((Element) eventNodes.item(i)));
        }
        return mappedEvents;
    }

    private MarketEvent mapEvent(Element eventElement) throws InvalidFileException {
        String eventName = getAttributeValue(eventElement, NAME_ATTRIBUTE);
        int eventId = getElementNumber(eventElement, ID_ELEMENT, eventName);
        String description = getElementText(eventElement, DESCRIPTION_ELEMENT, eventName);

        return new MarketEvent(eventId, eventName, description, mapOptions(eventElement),
                mapCommission(eventElement, eventName), mapTradingMethod(eventElement, eventName));
    }

    private CommissionConfig mapCommission(Element eventElement, String eventName) throws InvalidFileException {
        Element commissionElement = getSingleElement(eventElement, COMMISSION_ELEMENT, eventName);
        String policyValue = getAttributeValue(commissionElement, TYPE_ATTRIBUTE);

        CommissionPolicy policy = switch (policyValue) {
            case ON_PURCHASE_VALUE -> CommissionPolicy.ON_PURCHASE;
            case ON_CLOSE_VALUE -> CommissionPolicy.ON_CLOSE;
            default -> throw new UnknownCommissionTypeException(eventName, policyValue);
        };

        return new CommissionConfig(toNumber(eventName, COMMISSION_ELEMENT, textOf(commissionElement)), policy);
    }

    private List<MarketOption> mapOptions(Element eventElement) {
        List<MarketOption> options = new ArrayList<>();
        NodeList optionNodes = eventElement.getElementsByTagName(OPTION_ELEMENT);

        for (int i = 0; i < optionNodes.getLength(); i++) {
            options.add(new MarketOption(optionNodes.item(i).getTextContent().trim()));
        }
        return options;
    }

    private TradingMethod mapTradingMethod(Element eventElement, String eventName) throws InvalidFileException {
        Element methodElement = getSingleElement(eventElement, METHOD_ELEMENT, eventName);
        Element lmsrElement = getSingleElement(methodElement, LMSR_ELEMENT, eventName);
        int liquidityParameter = getElementNumber(lmsrElement, B_ELEMENT, eventName);

        try {
            return new LmsrTradingMethod(liquidityParameter);
        } catch (IllegalArgumentException e) {
            throw new InvalidLiquidityException(eventName, liquidityParameter);
        }
    }

    private Element getSingleElement(Element parent, String elementName, String eventName) throws MissingElementException {
        NodeList elements = parent.getElementsByTagName(elementName);

        if (elements.getLength() == 0) {
            throw new MissingElementException(eventName, elementName);
        }
        return (Element) elements.item(0);
    }

    private String getElementText(Element parent, String elementName, String eventName) throws MissingElementException {
        return textOf(getSingleElement(parent, elementName, eventName));
    }

    private int getElementNumber(Element parent, String elementName, String eventName) throws InvalidFileException {
        return toNumber(eventName, elementName, getElementText(parent, elementName, eventName));
    }

    private int toNumber(String eventName, String elementName, String text) throws NotANumberException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NotANumberException(eventName, elementName, text);
        }
    }

    private String textOf(Element element) {
        return element.getTextContent().trim();
    }

    private String getAttributeValue(Element element, String attributeName) throws MissingAttributeException {
        String value = element.getAttribute(attributeName).trim();

        if (value.isEmpty()) {
            throw new MissingAttributeException(element.getTagName(), attributeName);
        }
        return value;
    }
}
