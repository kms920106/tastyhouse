package com.tastyhouse.infrastructure.apple.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;

class AppleOAuthModuleAutoConfigurationTest {

    private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AppleOAuthModuleAutoConfiguration.class))
        .withBean(RestClient.Builder.class, RestClient::builder);

    private final ApplicationContextRunner runner = baseRunner
        .withPropertyValues(
            "oauth.apple.team-id=teamId-value",
            "oauth.apple.client-id=clientId-value",
            "oauth.apple.key-id=keyId-value",
            "oauth.apple.redirect-uri=redirectUri-value",
            "oauth.apple.private-key=privateKey-value"
        );

    @Test
    @DisplayName("oauth.apple.* 설정이 AppleOAuthProperties로 바인딩된다")
    void bindsProperties() {
        runner.run(context -> {
            AppleOAuthProperties properties = context.getBean(AppleOAuthProperties.class);
            assertThat(properties.teamId()).isEqualTo("teamId-value");
            assertThat(properties.clientId()).isEqualTo("clientId-value");
            assertThat(properties.keyId()).isEqualTo("keyId-value");
            assertThat(properties.redirectUri()).isEqualTo("redirectUri-value");
            assertThat(properties.privateKey()).isEqualTo("privateKey-value");
        });
    }

    @Test
    @DisplayName("appleOAuthClient 이름으로 APPLE 제공자 SocialOAuthClient 빈이 등록된다")
    void registersClientBeanUnderQualifierName() {
        runner.run(context -> {
            assertThat(context).hasBean("appleOAuthClient");
            SocialOAuthClient client = context.getBean("appleOAuthClient", SocialOAuthClient.class);
            assertThat(client.provider()).isEqualTo(SocialProvider.APPLE);
        });
    }

    @Test
    @DisplayName("설정 키가 없으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPropertyMissing() {
        baseRunner.withPropertyValues(
            "oauth.apple.client-id=clientId-value",
            "oauth.apple.key-id=keyId-value",
            "oauth.apple.redirect-uri=redirectUri-value",
            "oauth.apple.private-key=privateKey-value"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oauth.apple.team-id");
        });
    }

    @Test
    @DisplayName("환경변수가 해석되지 않으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPlaceholderUnresolved() {
        runner.withPropertyValues("oauth.apple.team-id=${OAUTH_TEST_UNDEFINED_VARIABLE}")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("oauth.apple.team-id")
                    .hasMessageContaining("OAUTH_TEST_UNDEFINED_VARIABLE");
            });
    }
}
