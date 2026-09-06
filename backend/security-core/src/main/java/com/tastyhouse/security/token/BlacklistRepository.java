package com.tastyhouse.security.token;

public interface BlacklistRepository {

    void add(String accessToken, long expirationMillis);

    boolean contains(String accessToken);
}
