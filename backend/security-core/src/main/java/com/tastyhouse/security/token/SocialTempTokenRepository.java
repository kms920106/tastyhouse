package com.tastyhouse.security.token;

public interface SocialTempTokenRepository {

    void save(SocialTempTokenProvider provider, String tempToken, String credential);

    String findCredential(SocialTempTokenProvider provider, String tempToken);

    void delete(SocialTempTokenProvider provider, String tempToken);
}
