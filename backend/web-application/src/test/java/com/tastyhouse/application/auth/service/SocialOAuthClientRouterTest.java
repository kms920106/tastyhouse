package com.tastyhouse.application.auth.service;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClientPort;
import com.tastyhouse.application.auth.port.out.SocialOAuthResult;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialOAuthClientRouterTest {

    @Test
    @DisplayName("요청한 provider의 클라이언트를 돌려준다")
    void resolve_returnsMatchingClient() {
        ClientStub kakao = new ClientStub(SocialProvider.KAKAO);
        ClientStub apple = new ClientStub(SocialProvider.APPLE);
        SocialOAuthClientRouter router = new SocialOAuthClientRouter(
            List.of(kakao, apple, new ClientStub(SocialProvider.NAVER), new ClientStub(SocialProvider.FACEBOOK)));

        assertThat(router.resolve(SocialProvider.KAKAO)).isSameAs(kakao);
        assertThat(router.resolve(SocialProvider.APPLE)).isSameAs(apple);
    }

    @Test
    @DisplayName("같은 provider의 클라이언트가 둘이면 생성 시점에 실패한다")
    void duplicateProvider_failsFast() {
        List<SocialOAuthClientPort> clients = List.of(
            new ClientStub(SocialProvider.KAKAO), new ClientStub(SocialProvider.APPLE),
            new ClientStub(SocialProvider.NAVER), new ClientStub(SocialProvider.NAVER), new ClientStub(SocialProvider.FACEBOOK));

        assertThatThrownBy(() -> new SocialOAuthClientRouter(clients))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("NAVER");
    }

    @Test
    @DisplayName("일부 provider가 누락되면 생성 시점에 누락된 provider를 담아 실패한다")
    void missingProvider_failsFast() {
        List<SocialOAuthClientPort> clients = List.of(new ClientStub(SocialProvider.KAKAO), new ClientStub(SocialProvider.APPLE));

        assertThatThrownBy(() -> new SocialOAuthClientRouter(clients))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("NAVER")
            .hasMessageContaining("FACEBOOK");
    }

    private static final class ClientStub implements SocialOAuthClientPort {

        private final SocialProvider provider;

        private ClientStub(SocialProvider provider) {
            this.provider = provider;
        }

        @Override
        public SocialProvider provider() {
            return provider;
        }

        @Override
        public SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization) {
            return SocialOAuthResult.success(SocialCredential.of(authorization.code()));
        }

        @Override
        public SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential) {
            return SocialOAuthResult.success(new SocialProfile(credential.value(), null, null, null, null, null, null, null, null, null));
        }
    }
}
