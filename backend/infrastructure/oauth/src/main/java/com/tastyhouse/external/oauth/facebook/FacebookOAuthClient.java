package com.tastyhouse.external.oauth.facebook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Component
public class FacebookOAuthClient implements SocialOAuthClient {

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";
    private static final String USER_FIELDS = "id,name,email,picture.type(large)";

    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    private final WebClient webClient;

    public FacebookOAuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.FACEBOOK;
    }

    @Override
    public SocialCredential exchange(SocialAuthorization authorization) {
        String facebookAccessToken = authorization.code();
        FacebookTokenDebugResponse debugResponse = debugToken(facebookAccessToken);
        if (!debugResponse.isValid() || !appId.equals(debugResponse.getAppId())) {
            throw new BusinessException(ErrorCode.SOCIAL_OAUTH_FAILED);
        }
        return SocialCredential.of(facebookAccessToken);
    }

    @Override
    public SocialProfile fetchProfile(SocialCredential credential) {
        FacebookUserInfoResponse user = fetchUserInfo(credential.value());
        return new SocialProfile(
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
        );
    }

    public FacebookUserInfoResponse fetchUserInfo(String facebookAccessToken) {
        return webClient.get()
            .uri(GRAPH_BASE_URL + "/me?fields=" + USER_FIELDS + "&access_token=" + facebookAccessToken)
            .retrieve()
            .bodyToMono(FacebookUserInfoResponse.class)
            .block();
    }

    public FacebookTokenDebugResponse debugToken(String facebookAccessToken) {
        String appAccessToken = appId + "|" + appSecret;
        return webClient.get()
            .uri(GRAPH_BASE_URL + "/debug_token?input_token=" + facebookAccessToken + "&access_token=" + appAccessToken)
            .retrieve()
            .bodyToMono(FacebookTokenDebugResponse.class)
            .block();
    }
}
