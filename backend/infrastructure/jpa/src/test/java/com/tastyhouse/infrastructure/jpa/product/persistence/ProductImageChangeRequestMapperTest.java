package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageChangeRequestMapperTest {

    @Test
    @DisplayName("ProductImageChangeRequest → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductImageChangeRequest productImageChangeRequest = ProductImageChangeRequest.reconstitute(
            91L,
            ProductId.of(92L),
            UploadedFileId.of(93L),
            ApprovalStatus.REJECTED,
            "흐림",
            LocalDateTime.of(2026, 2, 10, 10, 10),
            LocalDateTime.of(2026, 2, 11, 10, 11)
        );

        ProductImageChangeRequestJpaEntity entity = ProductImageChangeRequestMapper.toEntity(productImageChangeRequest);

        assertThat(entity.getProductId()).isEqualTo(92L);
        assertThat(entity.getImageFileId()).isEqualTo(93L);
        assertThat(entity.getStatus()).isEqualTo("REJECTED");
        assertThat(entity.getRejectReason()).isEqualTo("흐림");
    }

    @Test
    @DisplayName("엔티티 → ProductImageChangeRequest 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductImageChangeRequestJpaEntity entity = ProductImageChangeRequestJpaEntity.create(
            92L,
            93L,
            "REJECTED",
            "흐림"
        );
        ReflectionTestUtils.setField(entity, "id", 91L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 10, 10, 10));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 11, 10, 11));

        ProductImageChangeRequest restored = ProductImageChangeRequestMapper.toDomain(entity);

        ProductImageChangeRequest expected = ProductImageChangeRequest.reconstitute(
            91L,
            ProductId.of(92L),
            UploadedFileId.of(93L),
            ApprovalStatus.REJECTED,
            "흐림",
            LocalDateTime.of(2026, 2, 10, 10, 10),
            LocalDateTime.of(2026, 2, 11, 10, 11)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
