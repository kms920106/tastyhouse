package com.tastyhouse.external.oauth.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.external.oauth.spi.SocialAuthorization;
import com.tastyhouse.external.oauth.spi.SocialCredential;
import com.tastyhouse.external.oauth.spi.SocialOAuthClient;
import com.tastyhouse.external.oauth.spi.SocialProfile;
import com.tastyhouse.external.oauth.spi.SocialProvider;

@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements SocialOAuthClient {

    private static final String NAUTH_BASE_URL = "https://nid.naver.com";
    private static final String NAPI_BASE_URL = "https://openapi.naver.com";

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private final WebClient webClient;

    @Override
    public SocialProvider provider() {
        return SocialProvider.NAVER;
    }

    // 네이버만 state를 함께 넘겨 CSRF를 방어한다.
    @Override
    public SocialCredential exchange(SocialAuthorization authorization) {
        return SocialCredential.of(
            fetchToken(authorization.code(), authorization.state()).accessToken()
        );
    }

    @Override
    public SocialProfile fetchProfile(SocialCredential credential) {
        NaverUserInfoResponse user = fetchUserInfo(credential.value());
        return new SocialProfile(
            user.getProviderId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getName(),
            user.getMobile(),
            user.getGender(),
            user.getBirthYear(),
            user.getBirthMonth(),
            user.getBirthDay()
        );
    }

    // 인가 코드와 state로 네이버 액세스 토큰을 발급
    public NaverTokenResponse fetchToken(String authorizationCode, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", authorizationCode);
        formData.add("state", state);

        return webClient.post()
            .uri(NAUTH_BASE_URL + "/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(NaverTokenResponse.class)
            .block();
    }

    // 네이버 액세스 토큰으로 사용자 정보를 조회
    public NaverUserInfoResponse fetchUserInfo(String naverAccessToken) {
        return webClient.get()
            .uri(NAPI_BASE_URL + "/v1/nid/me")
            .header("Authorization", "Bearer " + naverAccessToken)
            .retrieve()
            .bodyToMono(NaverUserInfoResponse.class)
            .block();
    }
}
