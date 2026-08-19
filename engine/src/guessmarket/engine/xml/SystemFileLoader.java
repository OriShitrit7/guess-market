package guessmarket.engine.xml;

import guessmarket.engine.exception.FileDoesNotExistException;
import guessmarket.engine.exception.FileReadFailedException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.MalformedPathException;
import guessmarket.engine.exception.MalformedXmlException;
import guessmarket.engine.exception.NotAFileException;
import guessmarket.engine.exception.NotXmlFileException;
import guessmarket.engine.model.MarketEvent;
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

public class SystemFileLoader {
    private static final String JAXB_GENERATED_PACKAGE = "guessmarket.engine.xml.generated";
    private static final String XML_EXTENSION = ".xml";

    private final EventXmlMapper mapper = new EventXmlMapper();
    private final EventValidator validator = new EventValidator();

    public List<MarketEvent> load(String rawPath) throws InvalidFileException {
        Path path = validateFile(rawPath);
        GuessMarket xmlRoot = deserializeFrom(path);
        List<MarketEvent> events = mapper.mapEvents(xmlRoot);
        validator.validate(events);

        return events;
    }

    private GuessMarket deserializeFrom(Path path) throws InvalidFileException {
        try {
            JAXBContext context = JAXBContext.newInstance(JAXB_GENERATED_PACKAGE);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Object root = unmarshaller.unmarshal(path.toFile());

            if (!(root instanceof GuessMarket guessMarket)) {
                throw new MalformedXmlException(path);
            }
            return guessMarket;

        } catch (UnmarshalException e) {
            throw translateUnmarshalFailure(path, e);
        } catch (JAXBException e) {
            throw new MalformedXmlException(path);
        }
    }

    private InvalidFileException translateUnmarshalFailure(Path path, UnmarshalException e) {
        Throwable cause = e.getLinkedException();

        if (cause instanceof SAXParseException parseError) {
            return new MalformedXmlException(path, parseError.getLineNumber(),
                    parseError.getColumnNumber(), parseError.getMessage());
        }
        if (cause instanceof IOException) {
            return new FileReadFailedException(path);
        }
        return new MalformedXmlException(path);
    }

    private Path validateFile(String rawPath) throws InvalidFileException {
        String pathText = rawPath.trim();
        Path path;

        try {
            path = Path.of(pathText);
        } catch (InvalidPathException e) {
            throw new MalformedPathException(pathText);
        }

        if (!Files.exists(path)) {
            throw new FileDoesNotExistException(path);
        }
        if (!Files.isRegularFile(path)) {
            throw new NotAFileException(path);
        }
        if (!hasXmlExtension(path)) {
            throw new NotXmlFileException(path);
        }
        return path;
    }

    private boolean hasXmlExtension(Path path) {
        Path fileName = path.getFileName();

        return fileName != null && fileName.toString().toLowerCase(Locale.ROOT).endsWith(XML_EXTENSION);
    }
}
