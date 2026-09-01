package com.tastyhouse.external.oauth.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.webapplication.auth.port.out.SocialAuthorization;
import com.tastyhouse.webapplication.auth.port.out.SocialCredential;
import com.tastyhouse.webapplication.auth.port.out.SocialOAuthClient;
import com.tastyhouse.webapplication.auth.port.out.SocialProfile;
import com.tastyhouse.webapplication.auth.port.out.SocialProvider;

@Component
public class KakaoOAuthClient implements SocialOAuthClient {

    private static final String KAUTH_BASE_URL = "https://kauth.kakao.com";
    private static final String KAPI_BASE_URL = "https://kapi.kakao.com";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;

    public KakaoOAuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    // 인가 코드를 액세스 토큰으로 교환한다.
    @Override
    public SocialCredential exchange(SocialAuthorization authorization) {
        return SocialCredential.of(fetchToken(authorization.code()).accessToken());
    }

    @Override
    public SocialProfile fetchProfile(SocialCredential credential) {
        KakaoUserInfoResponse user = fetchUserInfo(credential.value());
        return new SocialProfile(
            String.valueOf(user.id()),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getName(),
            user.getPhoneNumber(),
            user.getGender(),
            null,
            null,
            null
        );
    }

    // 인가 코드로 카카오 액세스 토큰을 발급
    public KakaoTokenResponse fetchToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", authorizationCode);

        return webClient.post()
            .uri(KAUTH_BASE_URL + "/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(KakaoTokenResponse.class)
            .block();
    }

    // 카카오 액세스 토큰으로 사용자 정보를 조회
    public KakaoUserInfoResponse fetchUserInfo(String kakaoAccessToken) {
        return webClient.get()
            .uri(KAPI_BASE_URL + "/v2/user/me")
            .header("Authorization", "Bearer " + kakaoAccessToken)
            .retrieve()
            .bodyToMono(KakaoUserInfoResponse.class)
            .block();
    }
}
