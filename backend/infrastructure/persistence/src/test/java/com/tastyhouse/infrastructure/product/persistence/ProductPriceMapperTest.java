package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.product.port.out.write.ProductPriceState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProductPriceMapperTest {
    private static final LocalDateTime SET_AT = LocalDateTime.of(2026, 3, 1, 15, 0);

    @Test
    @DisplayName("인증 전 상태(매장가·픽업가·설정시각이 전부 null)를 State로 옮겨도 예외가 나지 않는다")
    void roundTrip_withUnverifiedNullColumns() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            10L,
            null,
            9000,
            null,
            null,
            0,
            null
        );

        assertThatCode(() -> ProductPriceMapper.toState(entity)).doesNotThrowAnyException();

        ProductPriceState state = ProductPriceMapper.toState(entity);
        assertThat(state.productId()).isEqualTo(10L);
        assertThat(state.priceName()).isNull();
        assertThat(state.deliveryPrice()).isEqualTo(9000);
        assertThat(state.storePrice()).isNull();
        assertThat(state.pickupPrice()).isNull();
        assertThat(state.pickupPriceSetAt()).isNull();
        assertThat(state.sort()).isZero();
    }

    @Test
    @DisplayName("인증 후 상태(전 채널 가격 + 설정시각)가 값 손실 없이 State로 옮겨진다")
    void roundTrip_withAllChannelPrices() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            10L, "곱빼기", 12000, 11000, 10500, 1, SET_AT);

        ProductPriceState state = ProductPriceMapper.toState(entity);

        assertThat(state.priceName()).isEqualTo("곱빼기");
        assertThat(state.deliveryPrice()).isEqualTo(12000);
        assertThat(state.storePrice()).isEqualTo(11000);
        assertThat(state.pickupPrice()).isEqualTo(10500);
        assertThat(state.pickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(state.sort()).isEqualTo(1);
    }

    @Test
    @DisplayName("State → 엔티티 변환도 채널 가격과 설정시각을 그대로 옮긴다")
    void toEntity_carriesAllFields() {
        ProductPriceState state = new ProductPriceState(
            5L, 10L, "보통", 9000, 8800, 8500, 0, SET_AT, null, null);

        ProductPriceJpaEntity entity = ProductPriceMapper.toEntity(state);

        assertThat(entity.getProductId()).isEqualTo(10L);
        assertThat(entity.getPriceName()).isEqualTo("보통");
        assertThat(entity.getDeliveryPrice()).isEqualTo(9000);
        assertThat(entity.getStorePrice()).isEqualTo(8800);
        assertThat(entity.getPickupPrice()).isEqualTo(8500);
        assertThat(entity.getPickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(entity.getSort()).isZero();
    }
}
