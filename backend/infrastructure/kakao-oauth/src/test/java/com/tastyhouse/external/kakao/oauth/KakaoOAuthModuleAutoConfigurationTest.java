package com.tastyhouse.external.kakao.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoOAuthModuleAutoConfigurationTest {

    private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KakaoOAuthModuleAutoConfiguration.class))
        .withBean(RestClient.Builder.class, RestClient::builder);

    private final ApplicationContextRunner runner = baseRunner
        .withPropertyValues(
            "oauth.kakao.client-id=clientId-value",
            "oauth.kakao.redirect-uri=redirectUri-value"
        );

    @Test
    @DisplayName("oauth.kakao.* 설정이 KakaoOAuthProperties로 바인딩된다")
    void bindsProperties() {
        runner.run(context -> {
            KakaoOAuthProperties properties = context.getBean(KakaoOAuthProperties.class);
            assertThat(properties.clientId()).isEqualTo("clientId-value");
            assertThat(properties.redirectUri()).isEqualTo("redirectUri-value");
        });
    }

    @Test
    @DisplayName("kakaoOAuthClient 이름으로 KAKAO 제공자 SocialOAuthClient 빈이 등록된다")
    void registersClientBeanUnderQualifierName() {
        runner.run(context -> {
            assertThat(context).hasBean("kakaoOAuthClient");
            SocialOAuthClient client = context.getBean("kakaoOAuthClient", SocialOAuthClient.class);
            assertThat(client.provider()).isEqualTo(SocialProvider.KAKAO);
        });
    }

    @Test
    @DisplayName("설정 키가 없으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPropertyMissing() {
        baseRunner.withPropertyValues(
            "oauth.kakao.redirect-uri=redirectUri-value"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oauth.kakao.client-id");
        });
    }

    @Test
    @DisplayName("환경변수가 해석되지 않으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPlaceholderUnresolved() {
        runner.withPropertyValues("oauth.kakao.client-id=${OAUTH_TEST_UNDEFINED_VARIABLE}")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("oauth.kakao.client-id")
                    .hasMessageContaining("OAUTH_TEST_UNDEFINED_VARIABLE");
            });
    }
}
