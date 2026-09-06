package com.tastyhouse.security.token;

public interface RefreshTokenRepository {

    void save(String username, String refreshToken, long ttlMillis);

    String find(String username);

    default boolean isInvalid(String username, String refreshToken) {
        return !refreshToken.equals(find(username));
    }

    void delete(String username);
}
