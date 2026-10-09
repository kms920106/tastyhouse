package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVegetarianRequestMapperTest {

    @Test
    @DisplayName("ProductVegetarianRequest → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductVegetarianRequest productVegetarianRequest = ProductVegetarianRequest.reconstitute(
            111L,
            ProductId.of(112L),
            VegetarianType.LACTO_OVO,
            "두부",
            "설명",
            ApprovalStatus.PENDING,
            "반려사유",
            LocalDateTime.of(2026, 2, 14, 10, 14),
            LocalDateTime.of(2026, 2, 15, 10, 15)
        );

        ProductVegetarianRequestJpaEntity entity = ProductVegetarianRequestMapper.toEntity(productVegetarianRequest);

        assertThat(entity.getProductId()).isEqualTo(112L);
        assertThat(entity.getVegetarianType()).isEqualTo("LACTO_OVO");
        assertThat(entity.getIngredients()).isEqualTo("두부");
        assertThat(entity.getDescription()).isEqualTo("설명");
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(entity.getRejectReason()).isEqualTo("반려사유");
    }

    @Test
    @DisplayName("엔티티 → ProductVegetarianRequest 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductVegetarianRequestJpaEntity entity = ProductVegetarianRequestJpaEntity.create(
            112L,
            "LACTO_OVO",
            "두부",
            "설명",
            "PENDING",
            "반려사유"
        );
        ReflectionTestUtils.setField(entity, "id", 111L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 14, 10, 14));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 15, 10, 15));

        ProductVegetarianRequest restored = ProductVegetarianRequestMapper.toDomain(entity);

        ProductVegetarianRequest expected = ProductVegetarianRequest.reconstitute(
            111L,
            ProductId.of(112L),
            VegetarianType.LACTO_OVO,
            "두부",
            "설명",
            ApprovalStatus.PENDING,
            "반려사유",
            LocalDateTime.of(2026, 2, 14, 10, 14),
            LocalDateTime.of(2026, 2, 15, 10, 15)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
