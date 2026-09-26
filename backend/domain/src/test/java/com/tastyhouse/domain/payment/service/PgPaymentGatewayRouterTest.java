package com.tastyhouse.domain.payment.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.PgProviderGateway;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgPaymentGatewayRouterTest {

    @Test
    @DisplayName("승인: 요청한 PG의 게이트웨이로 그대로 위임한다")
    void confirmPayment_routesToMatchingGateway() {
        GatewayStub toss = new GatewayStub(PgProvider.TOSS);
        GatewayStub kakao = new GatewayStub(PgProvider.KAKAO);
        PgPaymentGatewayRouter router = new PgPaymentGatewayRouter(List.of(toss, kakao));

        router.confirmPayment(PgProvider.KAKAO, 1L, "payment-key", "pg-order-1", 21000);

        assertThat(kakao.confirmCalls).containsExactly("pg-order-1");
        assertThat(toss.confirmCalls).isEmpty();
    }

    @Test
    @DisplayName("취소: 요청한 PG의 게이트웨이로 그대로 위임한다")
    void cancelPayment_routesToMatchingGateway() {
        GatewayStub toss = new GatewayStub(PgProvider.TOSS);
        PgPaymentGatewayRouter router = new PgPaymentGatewayRouter(List.of(toss));

        router.cancelPayment(PgProvider.TOSS, "tid-1", "고객 변심");

        assertThat(toss.cancelCalls).containsExactly("tid-1");
    }

    @Test
    @DisplayName("미등록 PG로 승인·취소를 요청하면 PG_PROVIDER_UNSUPPORTED로 거절한다")
    void unregisteredProvider_isRejected() {
        PgPaymentGatewayRouter router = new PgPaymentGatewayRouter(List.of(new GatewayStub(PgProvider.TOSS)));

        assertThatThrownBy(() -> router.confirmPayment(PgProvider.KAKAO, 1L, "payment-key", "pg-order-1", 21000))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PG_PROVIDER_UNSUPPORTED));
        assertThatThrownBy(() -> router.cancelPayment(null, "tid-1", "고객 변심"))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PG_PROVIDER_UNSUPPORTED));
    }

    @Test
    @DisplayName("supports: 등록된 PG만 true이고 미등록·null은 false다")
    void supports_reflectsRegisteredProviders() {
        PgPaymentGatewayRouter router = new PgPaymentGatewayRouter(List.of(new GatewayStub(PgProvider.TOSS)));

        assertThat(router.supports(PgProvider.TOSS)).isTrue();
        assertThat(router.supports(PgProvider.KAKAO)).isFalse();
        assertThat(router.supports(null)).isFalse();
    }

    @Test
    @DisplayName("게이트웨이가 하나도 없어도 생성되며 모든 PG를 지원하지 않는다")
    void emptyGateways_supportNothing() {
        PgPaymentGatewayRouter router = new PgPaymentGatewayRouter(List.of());

        assertThat(router.supports(PgProvider.TOSS)).isFalse();
    }

    @Test
    @DisplayName("같은 PG의 게이트웨이가 둘이면 생성 시점에 실패한다")
    void duplicateProvider_failsFast() {
        List<PgProviderGateway> gateways = List.of(new GatewayStub(PgProvider.TOSS), new GatewayStub(PgProvider.TOSS));

        assertThatThrownBy(() -> new PgPaymentGatewayRouter(gateways))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TOSS");
    }

    private static final class GatewayStub implements PgProviderGateway {
        private final PgProvider provider;
        private final List<String> confirmCalls = new ArrayList<>();
        private final List<String> cancelCalls = new ArrayList<>();

        private GatewayStub(PgProvider provider) {
            this.provider = provider;
        }

        @Override
        public PgProvider provider() {
            return provider;
        }

        @Override
        public PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount) {
            confirmCalls.add(pgOrderId);
            return new PgConfirmResult(true, paymentKey, "DONE", amount, null, null, null, null, null, null, null, null);
        }

        @Override
        public PgCancelResult cancelPayment(String pgTid, String cancelReason) {
            cancelCalls.add(pgTid);
            return new PgCancelResult(true, null, null);
        }
    }
}
