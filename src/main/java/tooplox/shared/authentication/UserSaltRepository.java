package tooplox.shared.authentication;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserSaltRepository {
    private final Map<String, String> userSalts = new HashMap<>();

    public String getOrCreateSalt(String userName, String password) {
        return userSalts.computeIfAbsent(userName + password, k -> generateSalt());
    }

    private String generateSalt() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
