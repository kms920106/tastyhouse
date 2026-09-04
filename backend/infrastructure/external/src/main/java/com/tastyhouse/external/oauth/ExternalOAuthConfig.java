package com.tastyhouse.external.oauth;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 소셜 로그인(OAuth) 클라이언트 진입점 설정 — 소셜 로그인을 쓰는 앱만 {@code @Import} 한다.
 *
 * <p>현재는 web-api 하나뿐이다. admin-api·ceo-api·batch-module은 소셜 로그인을 쓰지 않으므로
 * 이 설정을 import 하지 않으며, 그래서 OAuth 빈이 컨텍스트에 올라오지 않는다.
 *
 * <p>이 패키지의 빈들은 apple/kakao/naver/facebook 프로퍼티를 요구한다. 해당 프로퍼티는 web-api의
 * {@code application.yml}에만 있으므로, 다른 앱이 실수로 이 설정을 import 하면
 * {@code Could not resolve placeholder 'apple.team-id'}로 기동에 실패한다.
 *
 * <p>{@link com.tastyhouse.external.config.ExternalApiConfig}가 이 패키지를 제외하고 스캔하므로
 * 두 설정의 스캔 범위는 교집합이 없고, 둘 다 import 하면 external 모듈 전체가 된다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.external.oauth")
public class ExternalOAuthConfig {
}
