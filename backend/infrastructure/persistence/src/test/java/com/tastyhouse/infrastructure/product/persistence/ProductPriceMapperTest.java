package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProductPriceMapperTest {
    private static final LocalDateTime SET_AT = LocalDateTime.of(2026, 3, 1, 15, 0);

    @Test
    @DisplayName("인증 전 상태(매장가·픽업가·설정시각이 전부 null)를 도메인으로 옮겨도 예외가 나지 않는다")
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

        assertThatCode(() -> ProductPriceMapper.toDomain(entity)).doesNotThrowAnyException();

        ProductPrice price = ProductPriceMapper.toDomain(entity);
        assertThat(price.getProductId().value()).isEqualTo(10L);
        assertThat(price.getPriceName()).isNull();
        assertThat(price.getDeliveryPrice()).isEqualTo(9000);
        assertThat(price.getStorePrice()).isNull();
        assertThat(price.getPickupPrice()).isNull();
        assertThat(price.getPickupPriceSetAt()).isNull();
        assertThat(price.getSort()).isZero();
    }

    @Test
    @DisplayName("인증 후 상태(전 채널 가격 + 설정시각)가 값 손실 없이 도메인으로 옮겨진다")
    void roundTrip_withAllChannelPrices() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            10L, "곱빼기", 12000, 11000, 10500, 1, SET_AT);

        ProductPrice price = ProductPriceMapper.toDomain(entity);

        assertThat(price.getPriceName()).isEqualTo("곱빼기");
        assertThat(price.getDeliveryPrice()).isEqualTo(12000);
        assertThat(price.getStorePrice()).isEqualTo(11000);
        assertThat(price.getPickupPrice()).isEqualTo(10500);
        assertThat(price.getPickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(price.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("도메인 → 엔티티 변환도 채널 가격과 설정시각을 그대로 옮긴다")
    void toEntity_carriesAllFields() {
        ProductPrice price = ProductPrice.reconstitute(
            5L, ProductId.of(10L), "보통", 9000, 8800, 8500, 0, SET_AT, null, null);

        ProductPriceJpaEntity entity = ProductPriceMapper.toEntity(price);

        assertThat(entity.getProductId()).isEqualTo(10L);
        assertThat(entity.getPriceName()).isEqualTo("보통");
        assertThat(entity.getDeliveryPrice()).isEqualTo(9000);
        assertThat(entity.getStorePrice()).isEqualTo(8800);
        assertThat(entity.getPickupPrice()).isEqualTo(8500);
        assertThat(entity.getPickupPriceSetAt()).isEqualTo(SET_AT);
        assertThat(entity.getSort()).isZero();
    }

    @Test
    @DisplayName("ProductPrice → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductPrice productPrice = ProductPrice.reconstitute(
            131L,
            ProductId.of(132L),
            "곱빼기",
            12000,
            11000,
            10500,
            2,
            LocalDateTime.of(2026, 2, 16, 10, 16),
            LocalDateTime.of(2026, 2, 17, 10, 17),
            LocalDateTime.of(2026, 2, 18, 10, 18)
        );

        ProductPriceJpaEntity entity = ProductPriceMapper.toEntity(productPrice);

        assertThat(entity.getProductId()).isEqualTo(132L);
        assertThat(entity.getPriceName()).isEqualTo("곱빼기");
        assertThat(entity.getDeliveryPrice()).isEqualTo(12000);
        assertThat(entity.getStorePrice()).isEqualTo(11000);
        assertThat(entity.getPickupPrice()).isEqualTo(10500);
        assertThat(entity.getSort()).isEqualTo(2);
        assertThat(entity.getPickupPriceSetAt()).isEqualTo(LocalDateTime.of(2026, 2, 16, 10, 16));
    }

    @Test
    @DisplayName("엔티티 → ProductPrice 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductPriceJpaEntity entity = ProductPriceJpaEntity.create(
            132L,
            "곱빼기",
            12000,
            11000,
            10500,
            2,
            LocalDateTime.of(2026, 2, 16, 10, 16)
        );
        ReflectionTestUtils.setField(entity, "id", 131L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 17, 10, 17));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 18, 10, 18));

        ProductPrice restored = ProductPriceMapper.toDomain(entity);

        ProductPrice expected = ProductPrice.reconstitute(
            131L,
            ProductId.of(132L),
            "곱빼기",
            12000,
            11000,
            10500,
            2,
            LocalDateTime.of(2026, 2, 16, 10, 16),
            LocalDateTime.of(2026, 2, 17, 10, 17),
            LocalDateTime.of(2026, 2, 18, 10, 18)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
