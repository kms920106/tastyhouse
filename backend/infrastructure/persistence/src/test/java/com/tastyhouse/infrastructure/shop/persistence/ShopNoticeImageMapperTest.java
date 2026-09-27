package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;

import static org.assertj.core.api.Assertions.assertThat;

class ShopNoticeImageMapperTest {

    @Test
    @DisplayName("ShopNoticeImage 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopNoticeImage() {
        ShopNoticeImage original = ShopNoticeImage.reconstitute(
            108L,
            109L,
            UploadedFileId.of(110L),
            11
        );

        ShopNoticeImageJpaEntity entity = ShopNoticeImageMapper.toEntity(original);

        assertThat(entity.getShopNoticeId()).isEqualTo(109L);
        assertThat(entity.getImageFileId()).isEqualTo(110L);
        assertThat(entity.getSortOrder()).isEqualTo(11);
    }

    @Test
    @DisplayName("ShopNoticeImage 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopNoticeImage() {
        ShopNoticeImage original = ShopNoticeImage.reconstitute(
            108L,
            109L,
            UploadedFileId.of(110L),
            11
        );

        ShopNoticeImageJpaEntity entity = ShopNoticeImageJpaEntity.create(
            109L,
            110L,
            11
        );
        ReflectionTestUtils.setField(entity, "id", 108L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopNoticeImageMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
