package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

import static org.assertj.core.api.Assertions.assertThat;

class StorePriceVerificationItemMapperTest {

    @Test
    @DisplayName("StorePriceVerificationItem → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        StorePriceVerificationItem storePriceVerificationItem = StorePriceVerificationItem.reconstitute(
            241L,
            StorePriceVerificationId.of(242L),
            ProductId.of(243L),
            ProductPriceId.of(244L),
            9900,
            true,
            LocalDateTime.of(2026, 2, 27, 10, 27),
            LocalDateTime.of(2026, 2, 28, 10, 28)
        );

        StorePriceVerificationItemJpaEntity entity = StorePriceVerificationItemMapper.toEntity(storePriceVerificationItem);

        assertThat(entity.getVerificationId()).isEqualTo(242L);
        assertThat(entity.getProductId()).isEqualTo(243L);
        assertThat(entity.getProductPriceId()).isEqualTo(244L);
        assertThat(entity.getStorePrice()).isEqualTo(9900);
        assertThat(entity.isApplyPickupSamePrice()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → StorePriceVerificationItem 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        StorePriceVerificationItemJpaEntity entity = StorePriceVerificationItemJpaEntity.create(
            242L,
            243L,
            244L,
            9900,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 241L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 27, 10, 27));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 28, 10, 28));

        StorePriceVerificationItem restored = StorePriceVerificationItemMapper.toDomain(entity);

        StorePriceVerificationItem expected = StorePriceVerificationItem.reconstitute(
            241L,
            StorePriceVerificationId.of(242L),
            ProductId.of(243L),
            ProductPriceId.of(244L),
            9900,
            true,
            LocalDateTime.of(2026, 2, 27, 10, 27),
            LocalDateTime.of(2026, 2, 28, 10, 28)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
