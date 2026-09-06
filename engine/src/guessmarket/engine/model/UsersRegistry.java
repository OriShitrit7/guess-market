package guessmarket.engine.model;

import guessmarket.engine.exception.file.DuplicateUserNameException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsersRegistry {
    private final Map<String, User> users = new LinkedHashMap<>();

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUserByName(String username) {
        return users.get(username);
    }

    public void addUser(User user) throws DuplicateUserNameException {
        if (users.containsKey(user.getName())) {
            throw new DuplicateUserNameException(user.getName());
        }
        users.put(user.getName(), user);
    }
}


