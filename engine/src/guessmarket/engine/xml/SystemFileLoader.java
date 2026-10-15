package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.xml.generated.GuessMarket;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

// Loads system data from XML and coordinates file checks, mapping and validation.
public class SystemFileLoader {
    private static final String JAXB_GENERATED_PACKAGE = "guessmarket.engine.xml.generated";
    private static final String XML_EXTENSION = ".xml";

    private final EventXmlMapper mapper = new EventXmlMapper();
    private final MarketValidator validator = new MarketValidator();

    // Reads the events of an XML file and returns them once they pass validation.
    // Event IDs are assigned in file order, starting at nextEventId.
    public List<MarketEvent> load(String fileName, InputStream content, int nextEventId) throws InvalidFileException {
        validateXmlExtension(fileName); // Check the file type
        GuessMarket xmlRoot = deserializeFrom(content); // Read the JAXB structure
        List<MarketEvent> events = mapper.mapEvents(xmlRoot, nextEventId); // Build event models

        validator.validate(events); // Validate cross-object business rules

        return events;
    }

    private void validateXmlExtension(String fileName) throws InvalidFileException {
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(XML_EXTENSION)) {
            throw new InvalidFileException(FileErrorCode.NOT_XML_FILE, fileName);
        }
    }

    // Uses JAXB to convert the XML document into the generated object structure.
    private GuessMarket deserializeFrom(InputStream content) throws InvalidFileException {
        try {
            JAXBContext context = JAXBContext.newInstance(JAXB_GENERATED_PACKAGE); // Load generated JAXB classes
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // Stops at the first invalid value, such as text where a number is expected, instead of reading it as 0
            unmarshaller.setEventHandler(event -> event.getSeverity() == ValidationEvent.WARNING);
            Object root = unmarshaller.unmarshal(content);

            if (!(root instanceof GuessMarket guessMarket)) {
                throw new InvalidFileException(FileErrorCode.UNEXPECTED_XML_STRUCTURE);
            }
            return guessMarket;

        } catch (UnmarshalException e) {
            throw translateUnmarshalFailure(e);
        } catch (JAXBException e) {
            throw new InvalidFileException(FileErrorCode.MALFORMED_XML, e);
        }
    }

    // Converts JAXB failures into file exceptions that provide useful details to the user.
    // A missing linked exception means the document parsed but did not match the expected elements.
    private InvalidFileException translateUnmarshalFailure(UnmarshalException e) {
        Throwable cause = e.getLinkedException();

        if (cause instanceof SAXParseException parseError) {
            return new InvalidFileException(FileErrorCode.MALFORMED_XML_AT_LOCATION, parseError,
                    parseError.getLineNumber(), parseError.getColumnNumber(), parseError.getMessage()); // Include the XML location
        }
        if (cause instanceof NumberFormatException) {
            return new InvalidFileException(FileErrorCode.INVALID_NUMBER, cause, cause.getMessage());
        }
        if (cause instanceof IOException) {
            return new InvalidFileException(FileErrorCode.FILE_READ_FAILED, cause); // Preserve the read failure
        }
        return new InvalidFileException(FileErrorCode.UNEXPECTED_XML_STRUCTURE_DETAILS, e, e.getMessage());
    }
}
