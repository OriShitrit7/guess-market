package guessmarket.engine.xml;

import guessmarket.engine.exception.FileDoesNotExistException;
import guessmarket.engine.exception.FileReadFailedException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.MalformedPathException;
import guessmarket.engine.exception.MalformedXmlException;
import guessmarket.engine.exception.NotAFileException;
import guessmarket.engine.exception.NotXmlFileException;
import guessmarket.engine.model.MarketEvent;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class SystemFileLoader {
    private static final String XML_EXTENSION = ".xml";
    private final EventXmlMapper mapper = new EventXmlMapper();
    private final EventValidator validator = new EventValidator();

    public List<MarketEvent> load(String rawPath) throws InvalidFileException {
        Path path = validateFile(rawPath);
        Document document = parseDocument(path);
        List<MarketEvent> events = mapper.mapEvents(document);
        validator.validate(events);

        return events;

    }

    private Path validateFile(String rawPath) throws InvalidFileException {
        String pathText = rawPath.trim();
        Path path;

        try {
            path = Path.of(pathText);
        } catch (InvalidPathException e) {
            throw new MalformedPathException(pathText);
        }

        if (!hasXmlExtension(path)) {
            throw new NotXmlFileException(path);
        }
        if (!Files.exists(path)) {
            throw new FileDoesNotExistException(path);
        }
        if (!Files.isRegularFile(path)) {
            throw new NotAFileException(path);
        }
        return path;
    }

    private boolean hasXmlExtension(Path path) {
        Path fileName = path.getFileName();

        return fileName != null && fileName.toString().toLowerCase(Locale.ROOT).endsWith(XML_EXTENSION);
    }

    private Document parseDocument(Path path) throws InvalidFileException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.parse(path.toFile());

        } catch (SAXParseException e) {
            throw new MalformedXmlException(path, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            throw new MalformedXmlException(path);
        } catch (IOException e) {
            throw new FileReadFailedException(path);
        }
    }
}
