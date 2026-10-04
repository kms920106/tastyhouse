package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRequestIndexMapperTest {

    @Test
    @DisplayName("ShopRequestIndex 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopRequestIndex() {
        ShopRequestIndex original = ShopRequestIndex.reconstitute(
            119L,
            ShopId.of(120L),
            ShopRequestType.THUMBNAIL_CHANGE,
            122L,
            "v23",
            ShopRequestStatus.APPROVED,
            "v25",
            UploadedFileId.of(126L),
            127L,
            LocalDateTime.of(2026, 1, 1, 10, 28),
            LocalDateTime.of(2026, 1, 2, 10, 29),
            LocalDateTime.of(2026, 1, 3, 10, 30)
        );

        ShopRequestIndexJpaEntity entity = ShopRequestIndexMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(120L);
        assertThat(entity.getRequestType()).isEqualTo("THUMBNAIL_CHANGE");
        assertThat(entity.getSourceRequestId()).isEqualTo(122L);
        assertThat(entity.getSummary()).isEqualTo("v23");
        assertThat(entity.getStatus()).isEqualTo("APPROVED");
        assertThat(entity.getRejectReason()).isEqualTo("v25");
        assertThat(entity.getAttachmentFileId()).isEqualTo(126L);
        assertThat(entity.getRequestedByCeoId()).isEqualTo(127L);
        assertThat(entity.getProcessedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 28));
    }

    @Test
    @DisplayName("ShopRequestIndex 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopRequestIndex() {
        ShopRequestIndex original = ShopRequestIndex.reconstitute(
            119L,
            ShopId.of(120L),
            ShopRequestType.THUMBNAIL_CHANGE,
            122L,
            "v23",
            ShopRequestStatus.APPROVED,
            "v25",
            UploadedFileId.of(126L),
            127L,
            LocalDateTime.of(2026, 1, 1, 10, 28),
            LocalDateTime.of(2026, 1, 2, 10, 29),
            LocalDateTime.of(2026, 1, 3, 10, 30)
        );

        ShopRequestIndexJpaEntity entity = ShopRequestIndexJpaEntity.create(
            120L,
            "THUMBNAIL_CHANGE",
            122L,
            "v23",
            "APPROVED",
            "v25",
            126L,
            127L,
            LocalDateTime.of(2026, 1, 1, 10, 28)
        );
        ReflectionTestUtils.setField(entity, "id", 119L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 2, 10, 29));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 3, 10, 30));

        assertThat(ShopRequestIndexMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
