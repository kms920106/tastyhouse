package com.tastyhouse.security.token;

public interface NaverTempTokenRepository {

    void save(String naverTempToken, String naverAccessToken);

    String findNaverAccessToken(String naverTempToken);

    void delete(String naverTempToken);
}
