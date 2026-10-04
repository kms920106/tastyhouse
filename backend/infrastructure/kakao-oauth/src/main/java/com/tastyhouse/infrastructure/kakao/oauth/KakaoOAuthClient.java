package com.tastyhouse.infrastructure.kakao.oauth;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialOAuthResult;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Component
class KakaoOAuthClient implements SocialOAuthClient {

    private static final String KAUTH_BASE_URL = "https://kauth.kakao.com";
    private static final String KAPI_BASE_URL = "https://kapi.kakao.com";

    private final String clientId;

    private final String redirectUri;

    private final RestClient restClient;

    public KakaoOAuthClient(RestClient.Builder restClientBuilder, KakaoOAuthProperties properties) {
        this.restClient = restClientBuilder.build();
        this.clientId = properties.clientId();
        this.redirectUri = properties.redirectUri();
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization) {
        return SocialOAuthResult.success(SocialCredential.of(fetchToken(authorization.code()).accessToken()));
    }

    @Override
    public SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential) {
        KakaoUserInfoResponse user = fetchUserInfo(credential.value());
        return SocialOAuthResult.success(new SocialProfile(
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
        ));
    }

    public KakaoTokenResponse fetchToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", authorizationCode);

        return restClient.post()
            .uri(KAUTH_BASE_URL + "/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(KakaoTokenResponse.class);
    }

    public KakaoUserInfoResponse fetchUserInfo(String kakaoAccessToken) {
        return restClient.get()
            .uri(KAPI_BASE_URL + "/v2/user/me")
            .header("Authorization", "Bearer " + kakaoAccessToken)
            .retrieve()
            .body(KakaoUserInfoResponse.class);
    }
}
