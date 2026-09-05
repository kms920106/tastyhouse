package com.tastyhouse.external.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * infrastructure:oauth 모듈의 auto-configuration — 소셜 로그인(카카오·네이버·애플·페이스북) 클라이언트.
 *
 * <p>소셜 로그인을 쓰는 앱은 web-api 하나뿐이다. admin-api·ceo-api·batch-module은 이 모듈을
 * 의존하지 않으므로 jar가 클래스패스에 없고, 따라서 이 auto-configuration도 발화하지 않는다.
 *
 * <p>이 패키지의 빈들은 apple/kakao/naver/facebook 프로퍼티를 요구한다. 해당 프로퍼티는 web-api의
 * {@code application.yml}에만 있으므로, <b>다른 앱의 {@code build.gradle}에 이 모듈 의존을
 * 추가하면</b> 클래스패스 존재만으로 발화해 {@code Could not resolve placeholder 'apple.team-id'}로
 * 기동에 실패한다(batch-module 실패 이력). {@code @Import} 시절에는 의존 선언만으로는
 * 발화하지 않았으나, 지금은 <b>의존 선언 자체가 활성화</b>이므로 더 조심해야 한다.
 */
@AutoConfiguration
@ComponentScan("com.tastyhouse.external.oauth")
public class OAuthModuleAutoConfiguration {
}
