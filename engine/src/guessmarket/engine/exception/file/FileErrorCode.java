package guessmarket.engine.exception.file;

public enum FileErrorCode {
    MALFORMED_PATH("The file path is not valid on this system: '%s'."),
    FILE_NOT_FOUND("The file does not exist: '%s'."),
    NOT_A_FILE("The path does not point to a regular file: '%s'."),
    NOT_XML_FILE("The file '%s' does not have the required '.xml' extension."),
    FILE_READ_FAILED("The file '%s' could not be read. Check that it is accessible, not locked, and that you have permission to read it."),
    MALFORMED_XML("The file '%s' is not a well-formed XML document."),
    MALFORMED_XML_AT_LOCATION("The file '%s' is not a well-formed XML document.%nError at line %d, column %d: %s"),
    UNEXPECTED_XML_STRUCTURE("The file '%s' is well-formed XML, but it does not match the expected Guess Market structure. The root element must be <Guess-Market>."),
    UNEXPECTED_XML_STRUCTURE_DETAILS("The file '%s' is well-formed XML, but it does not match the expected Guess Market structure.%nDetails: %s"),
    MISSING_ELEMENT("Event '%s': the <%s> element is missing or empty."),
    MISSING_ATTRIBUTE("%s #%d in the file: the <%s> element must define a non-empty '%s' attribute."),
    NO_EVENTS("The file contains no events. At least one <GM-event> element is required."),
    NO_USERS("The file contains no users. At least one <GM-user> element is required."),
    INVALID_EVENT_ID("Event #%d in the file: <id> is missing or is not a positive integer."),
    DUPLICATE_EVENT_ID("Event '%s' has ID %d, which is already used by event '%s'. Event IDs must be unique."),
    DUPLICATE_USER_NAME("The user name '%s' appears more than once in the file. User names must be unique."),
    COMMISSION_OUT_OF_RANGE("Event '%s': the commission is %d%%. The allowed range is %d%% to %d%%, inclusive."),
    UNKNOWN_COMMISSION_TYPE("Event '%s': the 'action' attribute of <commission> has an unsupported value: '%s'. Expected 'on-purchase' or 'on-close'."),
    WRONG_OPTION_COUNT("Event '%s': the number of options is %d. Each event must have exactly %d options."),
    INVALID_LIQUIDITY("Event '%s': the <b> value %d is invalid. It must be greater than 0."),
    INVALID_INITIAL_BALANCE("User '%s' has an initial balance of %.2f. The initial balance must be greater than 0."),
    MARKET_MAKER_UNKNOWN_EVENT("User '%s' is defined as the market maker of event %d, which does not exist in the file."),
    MARKET_MAKER_ALREADY_ASSIGNED("Event '%s' already has market maker '%s', so user '%s' cannot be its market maker too."),
    MARKET_MAKER_MISSING("Event '%s' has no market maker. Every event must have exactly one."),
    INVALID_ORDER_BOOK_CONFIG("Event '%s': invalid Order Book settings (d=%s, initial=%s). d must be positive and initial must be non-negative and fit the supported share quantity.");

    private final String messageFormat;

    FileErrorCode(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String formatMessage(Object... parameters) {
        return String.format(messageFormat, parameters);
    }
}
