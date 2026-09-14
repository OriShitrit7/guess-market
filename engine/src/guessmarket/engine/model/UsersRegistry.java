package guessmarket.engine.model;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Stores the system's users by name.
public class UsersRegistry {
    private final Map<String, User> users = new LinkedHashMap<>();

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUserByName(String username) {
        return users.get(username);
    }

    // Adds a user unless another user already has the same name.
    public void addUser(User user) throws InvalidFileException {
        if (users.containsKey(user.getName())) {
            throw new InvalidFileException(FileErrorCode.DUPLICATE_USER_NAME, user.getName());
        }
        users.put(user.getName(), user);
    }
}
