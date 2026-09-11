package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.User;
import guessmarket.engine.xml.generated.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserXmlMapper {
    private static final String USER_ENTITY = "User";
    private static final String USER_ELEMENT = "GM-user";

    private static final String NAME_ATTRIBUTE = "name";

    public List<User> mapUsers(GuessMarket xmlRoot, List<MarketEvent> events) throws InvalidFileException {
        List<User> mappedUsers = new ArrayList<>();
        List<GMUser> xmlUsers = extractUsers(xmlRoot);
        Map<Integer, MarketEvent> eventsById = indexById(events);

        for (int i = 0; i < xmlUsers.size(); i++) {
            User user = mapUser(xmlUsers.get(i), i + 1);

            assignMarketMakerEvents(xmlUsers.get(i), user, eventsById);
            mappedUsers.add(user);
        }
        return mappedUsers;
    }

    private List<GMUser> extractUsers(GuessMarket xmlRoot) throws InvalidFileException {
        GMUsers usersElement = xmlRoot.getGMUsers();

        if (usersElement == null || usersElement.getGMUser().isEmpty()) {
            throw new InvalidFileException(FileErrorCode.NO_USERS);
        }
        return usersElement.getGMUser();
    }

    private User mapUser(GMUser xmlUser, int userNumber) throws InvalidFileException {
        String userName = xmlUser.getName();

        if (userName == null || userName.isBlank()) {
            throw new InvalidFileException(FileErrorCode.MISSING_ATTRIBUTE,
                    USER_ENTITY, userNumber, USER_ELEMENT, NAME_ATTRIBUTE);
        }
        return new User(userName.trim(), xmlUser.getInitialCash());
    }

    // Links every event the user declares to that user, rejecting unknown and already taken events.
    private void assignMarketMakerEvents(GMUser xmlUser, User user, Map<Integer, MarketEvent> eventsById)
            throws InvalidFileException {
        GMMarketMaker marketMakerElement = xmlUser.getGMMarketMaker();

        if (marketMakerElement == null) {
            return;
        }

        for (Event eventReference : marketMakerElement.getEvent()) {
            MarketEvent event = eventsById.get(eventReference.getId());

            if (event == null) {
                throw new InvalidFileException(FileErrorCode.MARKET_MAKER_UNKNOWN_EVENT,
                        user.getName(), eventReference.getId());
            }
            if (event.getMarketMakerName() != null) {
                throw new InvalidFileException(FileErrorCode.MARKET_MAKER_ALREADY_ASSIGNED,
                        event.getEventName(), event.getMarketMakerName(), user.getName());
            }
            event.setMarketMakerName(user.getName());
        }
    }

    private Map<Integer, MarketEvent> indexById(List<MarketEvent> events) {
        Map<Integer, MarketEvent> eventsById = new LinkedHashMap<>();

        for (MarketEvent event : events) {
            eventsById.put(event.getEventId(), event);
        }
        return eventsById;
    }
}
