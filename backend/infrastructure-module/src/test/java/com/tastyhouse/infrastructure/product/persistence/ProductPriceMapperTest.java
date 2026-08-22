package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * {@link ProductPriceMapper}의 round-trip 검증.
 *
 * <p><b>nullable 컬럼이 세 개(store_price·pickup_price·pickup_price_set_at)나 있는 것이 이 테스트의
 * 이유다.</b> 매장가·픽업가는 인증 전에는 비어 있는 것이 정상 상태이므로, 이관 직후의 전 메뉴가 이
 * 경로를 지난다 — 여기서 null을 잘못 다루면 가격 조회가 전부 깨진다.
 *
 * <p>{@code productId}는 NOT NULL FK지만 {@code IdMapping}을 경유하는지도 함께 본다(정책 B).
 */
class ProductPriceMapperTest {

    private static final LocalDateTime SET_AT = LocalDateTime.of(2026, 3, 1, 15, 0);

    @Test
    @DisplayName("인증 전 상태(매장가·픽업가·설정시각이 전부 null)를 round-trip해도 예외가 나지 않는다")
    void roundTrip_withUnverifiedNullColumns() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            10L,
            null, // priceName: 단일 가격
            9000,
            null, // storePrice: 인증 전
            null, // pickupPrice: 인증 전
            0,
            null  // pickupPriceSetAt
        );

        assertThatCode(() -> ProductPriceMapper.toDomain(entity)).doesNotThrowAnyException();

        ProductPrice domain = ProductPriceMapper.toDomain(entity);
        assertThat(domain.getProductId()).isEqualTo(ProductId.of(10L));
        assertThat(domain.getPriceName()).isNull();
        assertThat(domain.getDeliveryPrice()).isEqualTo(9000);
        assertThat(domain.getStorePrice()).isNull();
        assertThat(domain.getPickupPrice()).isNull();
        assertThat(domain.getPickupPriceSetAt()).isNull();
        assertThat(domain.getSort()).isZero();
    }

    @Test
    @DisplayName("인증 후 상태(전 채널 가격 + 설정시각)가 값 손실 없이 round-trip된다")
    void roundTrip_withAllChannelPrices() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            10L, "곱빼기", 12000, 11000, 10500, 1, SET_AT);

        ProductPrice domain = ProductPriceMapper.toDomain(entity);

        assertThat(domain.getPriceName()).isEqualTo("곱빼기");
        assertThat(domain.getDeliveryPrice()).isEqualTo(12000);
        assertThat(domain.getStorePrice()).isEqualTo(11000);
        assertThat(domain.getPickupPrice()).isEqualTo(10500);
        assertThat(domain.getPickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(domain.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("도메인 → 엔티티 변환도 채널 가격과 설정시각을 그대로 옮긴다")
    void toEntity_carriesAllFields() {
        ProductPrice domain = ProductPrice.reconstitute(
            5L, ProductId.of(10L), "보통", 9000, 8800, 8500, 0, SET_AT, null, null);

        ProductPriceJpaEntity entity = ProductPriceMapper.toEntity(domain);

        assertThat(entity.getProductId()).isEqualTo(10L);
        assertThat(entity.getPriceName()).isEqualTo("보통");
        assertThat(entity.getDeliveryPrice()).isEqualTo(9000);
        assertThat(entity.getStorePrice()).isEqualTo(8800);
        assertThat(entity.getPickupPrice()).isEqualTo(8500);
        assertThat(entity.getPickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(entity.getSort()).isZero();
    }
}
