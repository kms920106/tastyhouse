package com.tastyhouse.security.token;

public interface FacebookTempTokenRepository {

    void save(String facebookTempToken, String facebookAccessToken);

    String findFacebookAccessToken(String facebookTempToken);

    void delete(String facebookTempToken);
}
