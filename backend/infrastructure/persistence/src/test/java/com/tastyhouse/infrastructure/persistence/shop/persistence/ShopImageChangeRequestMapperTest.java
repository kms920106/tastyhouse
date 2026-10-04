package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopImageChangeRequestMapperTest {

    @Test
    @DisplayName("ShopImageChangeRequest 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopImageChangeRequest() {
        ShopImageChangeRequest original = ShopImageChangeRequest.reconstitute(
            131L,
            ShopId.of(132L),
            ShopImageType.THUMBNAIL,
            UploadedFileId.of(134L),
            ApprovalStatus.CANCELED,
            "v36",
            LocalDateTime.of(2026, 1, 10, 10, 37),
            LocalDateTime.of(2026, 1, 11, 10, 38)
        );

        ShopImageChangeRequestJpaEntity entity = ShopImageChangeRequestMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(132L);
        assertThat(entity.getImageType()).isEqualTo("THUMBNAIL");
        assertThat(entity.getImageFileId()).isEqualTo(134L);
        assertThat(entity.getStatus()).isEqualTo("CANCELED");
        assertThat(entity.getRejectReason()).isEqualTo("v36");
    }

    @Test
    @DisplayName("ShopImageChangeRequest 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopImageChangeRequest() {
        ShopImageChangeRequest original = ShopImageChangeRequest.reconstitute(
            131L,
            ShopId.of(132L),
            ShopImageType.THUMBNAIL,
            UploadedFileId.of(134L),
            ApprovalStatus.CANCELED,
            "v36",
            LocalDateTime.of(2026, 1, 10, 10, 37),
            LocalDateTime.of(2026, 1, 11, 10, 38)
        );

        ShopImageChangeRequestJpaEntity entity = ShopImageChangeRequestJpaEntity.create(
            132L,
            "THUMBNAIL",
            134L,
            "CANCELED",
            "v36"
        );
        ReflectionTestUtils.setField(entity, "id", 131L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 10, 10, 37));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 11, 10, 38));

        assertThat(ShopImageChangeRequestMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
