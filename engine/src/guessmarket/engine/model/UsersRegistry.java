package guessmarket.engine.model;

import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Stores the system's users by name. Names are compared without regard to case.
public class UsersRegistry {
    private final Map<String, User> users = new LinkedHashMap<>();

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    // Returns the user with the given name, or null when no such user exists.
    public User getUserByName(String username) {
        return username == null ? null : users.get(toKey(username));
    }

    // Adds a user unless another user already has the same name.
    public void addUser(User user) throws InvalidUserRequestException {
        String key = toKey(user.getName());

        if (users.containsKey(key)) {
            throw new InvalidUserRequestException(UserRequestErrorCode.USER_NAME_TAKEN);
        }
        users.put(key, user);
    }

    private static String toKey(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
