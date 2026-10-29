package com.tastyhouse.domain.payment.model;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMethodTest {

    @Test
    @DisplayName("현장 현금·현장 카드만 현장결제 수단이다")
    void onlyCashAndCardOnSiteAreOnSite() {
        assertThat(Arrays.stream(PaymentMethod.values()).filter(PaymentMethod::isOnSite))
            .containsExactlyInAnyOrder(PaymentMethod.CASH_ON_SITE, PaymentMethod.CARD_ON_SITE);
    }

    @Test
    @DisplayName("onSiteMethods는 현장결제 수단 목록을 돌려준다")
    void onSiteMethodsListsOnSiteMethods() {
        assertThat(PaymentMethod.onSiteMethods())
            .containsExactlyInAnyOrder(PaymentMethod.CASH_ON_SITE, PaymentMethod.CARD_ON_SITE);
    }
}
