package com.tastyhouse.external.facebook.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;

class FacebookOAuthModuleAutoConfigurationTest {

    private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FacebookOAuthModuleAutoConfiguration.class))
        .withBean(RestClient.Builder.class, RestClient::builder);

    private final ApplicationContextRunner runner = baseRunner
        .withPropertyValues(
            "oauth.facebook.app-id=appId-value",
            "oauth.facebook.app-secret=appSecret-value"
        );

    @Test
    @DisplayName("oauth.facebook.* 설정이 FacebookOAuthProperties로 바인딩된다")
    void bindsProperties() {
        runner.run(context -> {
            FacebookOAuthProperties properties = context.getBean(FacebookOAuthProperties.class);
            assertThat(properties.appId()).isEqualTo("appId-value");
            assertThat(properties.appSecret()).isEqualTo("appSecret-value");
        });
    }

    @Test
    @DisplayName("facebookOAuthClient 이름으로 FACEBOOK 제공자 SocialOAuthClient 빈이 등록된다")
    void registersClientBeanUnderQualifierName() {
        runner.run(context -> {
            assertThat(context).hasBean("facebookOAuthClient");
            SocialOAuthClient client = context.getBean("facebookOAuthClient", SocialOAuthClient.class);
            assertThat(client.provider()).isEqualTo(SocialProvider.FACEBOOK);
        });
    }

    @Test
    @DisplayName("설정 키가 없으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPropertyMissing() {
        baseRunner.withPropertyValues(
            "oauth.facebook.app-secret=appSecret-value"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oauth.facebook.app-id");
        });
    }

    @Test
    @DisplayName("환경변수가 해석되지 않으면 컨텍스트 기동이 실패한다")
    void failsStartupWhenPlaceholderUnresolved() {
        runner.withPropertyValues("oauth.facebook.app-id=${OAUTH_TEST_UNDEFINED_VARIABLE}")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("oauth.facebook.app-id")
                    .hasMessageContaining("OAUTH_TEST_UNDEFINED_VARIABLE");
            });
    }
}
