package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.User;
import guessmarket.engine.model.UsersRegistry;
import guessmarket.engine.xml.generated.GuessMarket;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

// Loads system data from XML and coordinates file checks, mapping and validation.
public class SystemFileLoader {
    private static final String JAXB_GENERATED_PACKAGE = "guessmarket.engine.xml.generated";
    private static final String XML_EXTENSION = ".xml";

    private final EventXmlMapper mapper = new EventXmlMapper();
    private final UserXmlMapper userMapper = new UserXmlMapper();
    private final MarketValidator validator = new MarketValidator();

    // Returns the loaded events and users only after the file and all mapped data pass validation.
    public LoadedSystemData load(String rawPath) throws InvalidFileException {
        Path path = validateFile(rawPath); // Validate the path and file type
        GuessMarket xmlRoot = deserializeFrom(path); // Read the JAXB structure
        List<MarketEvent> events = mapper.mapEvents(xmlRoot); // Build event models
        UsersRegistry users = buildRegistry(userMapper.mapUsers(xmlRoot, events)); // Build users and ownership

        validator.validate(events, users); // Validate cross-object business rules

        return new LoadedSystemData(events, users);
    }

    // Rejects duplicate names while collecting the mapped users into the registry.
    private UsersRegistry buildRegistry(List<User> mappedUsers) throws InvalidFileException {
        UsersRegistry registry = new UsersRegistry();

        for (User user : mappedUsers) {
            registry.addUser(user);
        }
        return registry;
    }

    // Uses JAXB to convert the XML document into the generated object structure.
    private GuessMarket deserializeFrom(Path path) throws InvalidFileException {
        try {
            JAXBContext context = JAXBContext.newInstance(JAXB_GENERATED_PACKAGE); // Load generated JAXB classes
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Object root = unmarshaller.unmarshal(path.toFile());

            if (!(root instanceof GuessMarket guessMarket)) {
                throw new InvalidFileException(FileErrorCode.UNEXPECTED_XML_STRUCTURE, path.getFileName());
            }
            return guessMarket;

        } catch (UnmarshalException e) {
            throw translateUnmarshalFailure(path, e);
        } catch (JAXBException e) {
            throw new InvalidFileException(FileErrorCode.MALFORMED_XML, e, path.getFileName());
        }
    }

    // Converts JAXB failures into file exceptions that provide useful details to the user.
    // A missing linked exception means the document parsed but did not match the expected elements.
    private InvalidFileException translateUnmarshalFailure(Path path, UnmarshalException e) {
        Throwable cause = e.getLinkedException();

        if (cause instanceof SAXParseException parseError) {
            return new InvalidFileException(FileErrorCode.MALFORMED_XML_AT_LOCATION, parseError,
                    path.getFileName(), parseError.getLineNumber(), parseError.getColumnNumber(), parseError.getMessage()); // Include the XML location
        }
        if (cause instanceof IOException) {
            return new InvalidFileException(FileErrorCode.FILE_READ_FAILED, cause, path); // Preserve the read failure
        }
        return new InvalidFileException(FileErrorCode.UNEXPECTED_XML_STRUCTURE_DETAILS, e, path.getFileName(), e.getMessage());
    }

    // Validates the path, file type and XML extension before attempting to read the document.
    private Path validateFile(String rawPath) throws InvalidFileException {
        String pathText = rawPath.trim();
        Path path;

        try {
            path = Path.of(pathText);
        } catch (InvalidPathException e) {
            throw new InvalidFileException(FileErrorCode.MALFORMED_PATH, e, pathText);
        }

        if (!Files.exists(path)) {
            throw new InvalidFileException(FileErrorCode.FILE_NOT_FOUND, path);
        }
        if (!Files.isRegularFile(path)) {
            throw new InvalidFileException(FileErrorCode.NOT_A_FILE, path);
        }
        if (!hasXmlExtension(path)) {
            Path fileName = path.getFileName();
            throw new InvalidFileException(FileErrorCode.NOT_XML_FILE, fileName != null ? fileName : path);
        }
        return path;
    }

    private boolean hasXmlExtension(Path path) {
        Path fileName = path.getFileName();

        return fileName != null && fileName.toString().toLowerCase(Locale.ROOT).endsWith(XML_EXTENSION);
    }
}
