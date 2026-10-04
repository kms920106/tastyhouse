package com.tastyhouse.infrastructure.naver.oauth;

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
class NaverOAuthClient implements SocialOAuthClient {

    private static final String NAUTH_BASE_URL = "https://nid.naver.com";
    private static final String NAPI_BASE_URL = "https://openapi.naver.com";

    private final String clientId;

    private final String clientSecret;

    private final String redirectUri;

    private final RestClient restClient;

    public NaverOAuthClient(RestClient.Builder restClientBuilder, NaverOAuthProperties properties) {
        this.restClient = restClientBuilder.build();
        this.clientId = properties.clientId();
        this.clientSecret = properties.clientSecret();
        this.redirectUri = properties.redirectUri();
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.NAVER;
    }

    @Override
    public SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization) {
        return SocialOAuthResult.success(SocialCredential.of(
            fetchToken(authorization.code(), authorization.state()).accessToken()
        ));
    }

    @Override
    public SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential) {
        NaverUserInfoResponse user = fetchUserInfo(credential.value());
        return SocialOAuthResult.success(new SocialProfile(
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
        ));
    }

    public NaverTokenResponse fetchToken(String authorizationCode, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", authorizationCode);
        formData.add("state", state);
        formData.add("redirect_uri", redirectUri);

        return restClient.post()
            .uri(NAUTH_BASE_URL + "/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(NaverTokenResponse.class);
    }

    public NaverUserInfoResponse fetchUserInfo(String naverAccessToken) {
        return restClient.get()
            .uri(NAPI_BASE_URL + "/v1/nid/me")
            .header("Authorization", "Bearer " + naverAccessToken)
            .retrieve()
            .body(NaverUserInfoResponse.class);
    }
}
