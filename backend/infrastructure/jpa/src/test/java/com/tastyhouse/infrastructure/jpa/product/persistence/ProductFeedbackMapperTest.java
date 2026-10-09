package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackMapperTest {

    @Test
    @DisplayName("ProductFeedback → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductFeedback productFeedback = ProductFeedback.reconstitute(
            61L,
            ProductId.of(62L),
            ShopId.of(63L),
            MemberId.of(64L),
            ProductFeedbackType.SOLD_OUT,
            "품절이 잦아요",
            LocalDateTime.of(2026, 2, 8, 10, 8),
            LocalDateTime.of(2026, 2, 9, 10, 9)
        );

        ProductFeedbackJpaEntity entity = ProductFeedbackMapper.toEntity(productFeedback);

        assertThat(entity.getProductId()).isEqualTo(62L);
        assertThat(entity.getShopId()).isEqualTo(63L);
        assertThat(entity.getMemberId()).isEqualTo(64L);
        assertThat(entity.getFeedbackType()).isEqualTo("SOLD_OUT");
        assertThat(entity.getContent()).isEqualTo("품절이 잦아요");
    }

    @Test
    @DisplayName("엔티티 → ProductFeedback 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductFeedbackJpaEntity entity = ProductFeedbackJpaEntity.create(
            62L,
            63L,
            64L,
            "SOLD_OUT",
            "품절이 잦아요"
        );
        ReflectionTestUtils.setField(entity, "id", 61L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 8, 10, 8));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 9, 10, 9));

        ProductFeedback restored = ProductFeedbackMapper.toDomain(entity);

        ProductFeedback expected = ProductFeedback.reconstitute(
            61L,
            ProductId.of(62L),
            ShopId.of(63L),
            MemberId.of(64L),
            ProductFeedbackType.SOLD_OUT,
            "품절이 잦아요",
            LocalDateTime.of(2026, 2, 8, 10, 8),
            LocalDateTime.of(2026, 2, 9, 10, 9)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
