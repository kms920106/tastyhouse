package com.tastyhouse.external.oauth.facebook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapplication.auth.port.out.SocialAuthorization;
import com.tastyhouse.webapplication.auth.port.out.SocialCredential;
import com.tastyhouse.webapplication.auth.port.out.SocialOAuthClient;
import com.tastyhouse.webapplication.auth.port.out.SocialProfile;
import com.tastyhouse.webapplication.auth.port.out.SocialProvider;

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

    // 페이스북은 JS SDK가 이미 액세스 토큰을 발급했으므로 교환할 것이 없다. 대신 Facebook 공식 문서가
    // 요구하는 서버측 검증(토큰의 app_id가 우리 앱과 일치하는지)을 여기서 수행하고 토큰을 그대로 돌려준다.
    // 이 검증은 과거 web-api의 FacebookSocialLoginService#validateToken에 있었으나, app_id 설정값과
    // debug_token 호출은 어댑터의 관심사이므로 이 모듈로 회수했다.
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

    // JS SDK가 발급한 액세스 토큰으로 사용자 정보를 조회
    // Facebook JS SDK 방식은 클라이언트가 액세스 토큰을 직접 발급받으므로 토큰 교환 단계가 없다.
    public FacebookUserInfoResponse fetchUserInfo(String facebookAccessToken) {
        return webClient.get()
            .uri(GRAPH_BASE_URL + "/me?fields=" + USER_FIELDS + "&access_token=" + facebookAccessToken)
            .retrieve()
            .bodyToMono(FacebookUserInfoResponse.class)
            .block();
    }

    // 서버에서 액세스 토큰의 유효성을 검증 (app_id, user_id 확인)
    public FacebookTokenDebugResponse debugToken(String facebookAccessToken) {
        String appAccessToken = appId + "|" + appSecret;
        return webClient.get()
            .uri(GRAPH_BASE_URL + "/debug_token?input_token=" + facebookAccessToken + "&access_token=" + appAccessToken)
            .retrieve()
            .bodyToMono(FacebookTokenDebugResponse.class)
            .block();
    }
}
