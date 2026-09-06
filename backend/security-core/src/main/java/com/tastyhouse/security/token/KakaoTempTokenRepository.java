package com.tastyhouse.security.token;

public interface KakaoTempTokenRepository {

    void save(String kakaoTempToken, String kakaoAccessToken);

    String findKakaoAccessToken(String kakaoTempToken);

    void delete(String kakaoTempToken);
}
