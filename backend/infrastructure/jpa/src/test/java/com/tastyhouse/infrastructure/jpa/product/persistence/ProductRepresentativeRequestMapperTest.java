package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepresentativeRequestMapperTest {

    @Test
    @DisplayName("ProductRepresentativeRequest → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductRepresentativeRequest productRepresentativeRequest = ProductRepresentativeRequest.reconstitute(
            101L,
            ProductId.of(102L),
            ShopId.of(103L),
            ApprovalStatus.APPROVED,
            "사유",
            LocalDateTime.of(2026, 2, 12, 10, 12),
            LocalDateTime.of(2026, 2, 13, 10, 13)
        );

        ProductRepresentativeRequestJpaEntity entity = ProductRepresentativeRequestMapper.toEntity(productRepresentativeRequest);

        assertThat(entity.getProductId()).isEqualTo(102L);
        assertThat(entity.getShopId()).isEqualTo(103L);
        assertThat(entity.getStatus()).isEqualTo("APPROVED");
        assertThat(entity.getRejectReason()).isEqualTo("사유");
    }

    @Test
    @DisplayName("엔티티 → ProductRepresentativeRequest 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductRepresentativeRequestJpaEntity entity = ProductRepresentativeRequestJpaEntity.create(
            102L,
            103L,
            "APPROVED",
            "사유"
        );
        ReflectionTestUtils.setField(entity, "id", 101L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 12, 10, 12));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 13, 10, 13));

        ProductRepresentativeRequest restored = ProductRepresentativeRequestMapper.toDomain(entity);

        ProductRepresentativeRequest expected = ProductRepresentativeRequest.reconstitute(
            101L,
            ProductId.of(102L),
            ShopId.of(103L),
            ApprovalStatus.APPROVED,
            "사유",
            LocalDateTime.of(2026, 2, 12, 10, 12),
            LocalDateTime.of(2026, 2, 13, 10, 13)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
