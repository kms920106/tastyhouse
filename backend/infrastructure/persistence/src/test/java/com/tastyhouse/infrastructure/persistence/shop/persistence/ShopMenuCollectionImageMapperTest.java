package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopMenuCollectionImageMapperTest {

    @Test
    @DisplayName("ShopMenuCollectionImage 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopMenuCollectionImage() {
        ShopMenuCollectionImage original = ShopMenuCollectionImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42,
            ApprovalStatus.CANCELED,
            "v44",
            LocalDateTime.of(2026, 1, 18, 10, 45),
            LocalDateTime.of(2026, 1, 19, 10, 46)
        );

        ShopMenuCollectionImageJpaEntity entity = ShopMenuCollectionImageMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(140L);
        assertThat(entity.getImageFileId()).isEqualTo(141L);
        assertThat(entity.getSort()).isEqualTo(42);
        assertThat(entity.getStatus()).isEqualTo("CANCELED");
        assertThat(entity.getRejectReason()).isEqualTo("v44");
    }

    @Test
    @DisplayName("ShopMenuCollectionImage 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopMenuCollectionImage() {
        ShopMenuCollectionImage original = ShopMenuCollectionImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42,
            ApprovalStatus.CANCELED,
            "v44",
            LocalDateTime.of(2026, 1, 18, 10, 45),
            LocalDateTime.of(2026, 1, 19, 10, 46)
        );

        ShopMenuCollectionImageJpaEntity entity = ShopMenuCollectionImageJpaEntity.create(
            140L,
            141L,
            42,
            "CANCELED",
            "v44"
        );
        ReflectionTestUtils.setField(entity, "id", 139L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 18, 10, 45));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 19, 10, 46));

        assertThat(ShopMenuCollectionImageMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
