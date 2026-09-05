package com.tastyhouse.external.oauth;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * infrastructure:oauth 모듈의 진입점 설정 — 소셜 로그인(카카오·네이버·애플·페이스북) 클라이언트.
 *
 * <p>소셜 로그인을 쓰는 앱은 web-api 하나뿐이다. admin-api·ceo-api·batch-module은 이 모듈을
 * 의존하지도 import 하지도 않으므로 OAuth 빈이 컨텍스트에 올라오지 않는다.
 *
 * <p>이 패키지의 빈들은 apple/kakao/naver/facebook 프로퍼티를 요구한다. 해당 프로퍼티는 web-api의
 * {@code application.yml}에만 있으므로, 다른 앱이 실수로 이 설정을 import 하면
 * {@code Could not resolve placeholder 'apple.team-id'}로 기동에 실패한다(batch-module 실패 이력).
 * 분리 전에는 코어 {@code ExternalModuleConfig}가 이 패키지를 REGEX {@code excludeFilters}로
 * 제외해서 같은 효과를 냈으나, 이제는 모듈 경계가 그 역할을 대신한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.external.oauth")
public class OAuthModuleConfig {
}
