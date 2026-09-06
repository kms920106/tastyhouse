package com.tastyhouse.security.token;

public interface AppleTempTokenRepository {

    void save(String appleTempToken, String appleIdToken);

    String findAppleIdToken(String appleTempToken);

    void delete(String appleTempToken);
}
