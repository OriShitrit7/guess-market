package guessmarket.engine.exception;

import java.nio.file.Path;

// Reports a file that parses as XML but does not describe a Guess Market system.
public class UnexpectedXmlStructureException extends InvalidFileException {
    private static final String ROOT_ELEMENT = "Guess-Market";
    private static final String MSG_FORMAT =
            "The file '%s' is well-formed XML, but it does not match the expected Guess Market structure.";
    private static final String ROOT_FORMAT = "%s The root element must be <%s>.";
    private static final String DETAIL_FORMAT = "%s%nDetails: %s";

    private final Path path;
    private final String reason;

    public UnexpectedXmlStructureException(Path path) {
        this(path, null);
    }

    public UnexpectedXmlStructureException(Path path, String reason) {
        this.path = path;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        String message = String.format(MSG_FORMAT, path.getFileName());

        // The parser detail names the offending element, so it replaces the general root element hint.
        return reason == null ? String.format(ROOT_FORMAT, message, ROOT_ELEMENT)
                : String.format(DETAIL_FORMAT, message, reason);
    }
}
