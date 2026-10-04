package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackReadMapperTest {

    @Test
    @DisplayName("ProductFeedbackRead → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductFeedbackRead productFeedbackRead = ProductFeedbackRead.reconstitute(
            51L,
            ShopId.of(52L),
            LocalDateTime.of(2026, 2, 5, 10, 5),
            LocalDateTime.of(2026, 2, 6, 10, 6),
            LocalDateTime.of(2026, 2, 7, 10, 7)
        );

        ProductFeedbackReadJpaEntity entity = ProductFeedbackReadMapper.toEntity(productFeedbackRead);

        assertThat(entity.getShopId()).isEqualTo(52L);
        assertThat(entity.getReadAt()).isEqualTo(LocalDateTime.of(2026, 2, 5, 10, 5));
    }

    @Test
    @DisplayName("엔티티 → ProductFeedbackRead 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductFeedbackReadJpaEntity entity = ProductFeedbackReadJpaEntity.create(
            52L,
            LocalDateTime.of(2026, 2, 5, 10, 5)
        );
        ReflectionTestUtils.setField(entity, "id", 51L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 6, 10, 6));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 7, 10, 7));

        ProductFeedbackRead restored = ProductFeedbackReadMapper.toDomain(entity);

        ProductFeedbackRead expected = ProductFeedbackRead.reconstitute(
            51L,
            ShopId.of(52L),
            LocalDateTime.of(2026, 2, 5, 10, 5),
            LocalDateTime.of(2026, 2, 6, 10, 6),
            LocalDateTime.of(2026, 2, 7, 10, 7)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
