package com.tastyhouse.external.facebook.oauth;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialOAuthFailure;
import com.tastyhouse.application.auth.port.out.SocialOAuthResult;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Component
public class FacebookOAuthClient implements SocialOAuthClient {

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";
    private static final String USER_FIELDS = "id,name,email,picture.type(large)";

    private final String appId;

    private final String appSecret;

    private final RestClient restClient;

    public FacebookOAuthClient(RestClient.Builder restClientBuilder, FacebookOAuthProperties properties) {
        this.restClient = restClientBuilder.build();
        this.appId = properties.appId();
        this.appSecret = properties.appSecret();
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.FACEBOOK;
    }

    @Override
    public SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization) {
        String facebookAccessToken = authorization.code();
        FacebookTokenDebugResponse debugResponse = debugToken(facebookAccessToken);
        if (!debugResponse.isValid() || !appId.equals(debugResponse.getAppId())) {
            return SocialOAuthResult.failed(SocialOAuthFailure.ACCESS_TOKEN_REJECTED);
        }
        return SocialOAuthResult.success(SocialCredential.of(facebookAccessToken));
    }

    @Override
    public SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential) {
        FacebookUserInfoResponse user = fetchUserInfo(credential.value());
        return SocialOAuthResult.success(new SocialProfile(
            user.id(),
            user.email(),
            null,
            user.getProfileImageUrl(),
            user.name(),
            null,
            null,
            null,
            null,
            null
        ));
    }

    public FacebookUserInfoResponse fetchUserInfo(String facebookAccessToken) {
        return restClient.get()
            .uri(GRAPH_BASE_URL + "/me?fields={fields}&access_token={accessToken}", USER_FIELDS, facebookAccessToken)
            .retrieve()
            .body(FacebookUserInfoResponse.class);
    }

    public FacebookTokenDebugResponse debugToken(String facebookAccessToken) {
        String appAccessToken = appId + "|" + appSecret;
        return restClient.get()
            .uri(
                GRAPH_BASE_URL + "/debug_token?input_token={inputToken}&access_token={accessToken}",
                facebookAccessToken,
                appAccessToken
            )
            .retrieve()
            .body(FacebookTokenDebugResponse.class);
    }
}
