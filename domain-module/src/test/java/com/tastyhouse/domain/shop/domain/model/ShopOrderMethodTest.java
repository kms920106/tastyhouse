package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOrderMethodTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientShopOrderMethod() {
        ShopOrderMethod orderMethod = ShopOrderMethod.of(1L, OrderMethod.DELIVERY);

        assertThat(orderMethod.getId()).isNull();
        assertThat(orderMethod.getShopId()).isEqualTo(1L);
        assertThat(orderMethod.getOrderMethod()).isEqualTo(OrderMethod.DELIVERY);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopOrderMethod orderMethod = ShopOrderMethod.reconstitute(1L, 2L, OrderMethod.DELIVERY);

        assertThat(orderMethod.getId()).isEqualTo(1L);
        assertThat(orderMethod.getShopId()).isEqualTo(2L);
    }
}
