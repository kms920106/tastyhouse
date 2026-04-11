package com.tastyhouse.external.oauth.facebook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class FacebookOAuthClient {

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";
    private static final String USER_FIELDS = "id,name,email,picture.type(large)";

    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    private final WebClient webClient;

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
