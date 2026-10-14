package guessmarket.engine.exception.file;

public enum FileErrorCode {
    NOT_XML_FILE("The file '%s' does not have the required '.xml' extension."),
    FILE_READ_FAILED("The file could not be read."),
    MALFORMED_XML("The file is not a well-formed XML document."),
    MALFORMED_XML_AT_LOCATION("The file is not a well-formed XML document.%nError at line %d, column %d: %s"),
    UNEXPECTED_XML_STRUCTURE("The file is well-formed XML, but it does not match the expected Guess Market structure. The root element must be <Guess-Market>."),
    UNEXPECTED_XML_STRUCTURE_DETAILS("The file is well-formed XML, but it does not match the expected Guess Market structure.%nDetails: %s"),
    MISSING_ELEMENT("Event '%s': the <%s> element is missing or empty."),
    MISSING_ATTRIBUTE("%s #%d in the file: the <%s> element must define a non-empty '%s' attribute."),
    NO_EVENTS("The file contains no events. At least one <GM-event> element is required."),
    DUPLICATE_EVENT_NAME("The event name '%s' appears more than once in the file. Event names must be unique (case insensitive)."),
    EVENT_NAME_ALREADY_EXISTS("An event named '%s' already exists in the system. Event names must be unique (case insensitive)."),
    COMMISSION_OUT_OF_RANGE("Event '%s': the commission is %d%%. The allowed range is %d%% to %d%%, inclusive."),
    UNKNOWN_COMMISSION_TYPE("Event '%s': the 'type' attribute of <commission> has an unsupported value: '%s'. Expected 'on-purchase' or 'on-close'."),
    WRONG_OPTION_COUNT("Event '%s': the number of options is %d. Each event must have exactly %d options."),
    INVALID_LIQUIDITY("Event '%s': the <b> value %d is invalid. It must be greater than 0."),
    INVALID_ORDER_BOOK_CONFIG("Event '%s': invalid Order Book settings (d=%s, initial=%s). d must be positive and initial must be non-negative and fit the supported share quantity.");

    private final String messageFormat;

    FileErrorCode(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String formatMessage(Object... parameters) {
        return String.format(messageFormat, parameters);
    }
}
