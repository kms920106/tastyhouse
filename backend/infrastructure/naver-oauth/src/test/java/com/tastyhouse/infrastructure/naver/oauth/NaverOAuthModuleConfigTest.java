package com.tastyhouse.infrastructure.naver.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;

class NaverOAuthModuleConfigTest {

    private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
        .withUserConfiguration(NaverOAuthModuleConfig.class, NaverOAuthClient.class)
        .withBean(RestClient.Builder.class, RestClient::builder);

    private final ApplicationContextRunner runner = baseRunner
        .withPropertyValues(
            "oauth.naver.client-id=clientId-value",
            "oauth.naver.client-secret=clientSecret-value",
            "oauth.naver.redirect-uri=redirectUri-value"
        );

    @Test
    @DisplayName("oauth.naver.* 설정이 NaverOAuthProperties로 바인딩된다")
    void bindsProperties() {
        runner.run(context -> {
            NaverOAuthProperties properties = context.getBean(NaverOAuthProperties.class);
            assertThat(properties.clientId()).isEqualTo("clientId-value");
            assertThat(properties.clientSecret()).isEqualTo("clientSecret-value");
            assertThat(properties.redirectUri()).isEqualTo("redirectUri-value");
        });
    }

    @Test
    @DisplayName("naverOAuthClient 이름으로 NAVER 제공자 SocialOAuthClient 빈이 등록된다")
    void registersClientBeanUnderQualifierName() {
        runner.run(context -> {
            assertThat(context).hasBean("naverOAuthClient");
            SocialOAuthClient client = context.getBean("naverOAuthClient", SocialOAuthClient.class);
            assertThat(client.provider()).isEqualTo(SocialProvider.NAVER);
        });
    }

    @Test
    @DisplayName("설정 키가 없으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPropertyMissing() {
        baseRunner.withPropertyValues(
            "oauth.naver.client-secret=clientSecret-value",
            "oauth.naver.redirect-uri=redirectUri-value"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oauth.naver.client-id");
        });
    }

    @Test
    @DisplayName("환경변수가 해석되지 않으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPlaceholderUnresolved() {
        runner.withPropertyValues("oauth.naver.client-id=${OAUTH_TEST_UNDEFINED_VARIABLE}")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("oauth.naver.client-id")
                    .hasMessageContaining("OAUTH_TEST_UNDEFINED_VARIABLE");
            });
    }
}
