package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class StorePriceVerificationMapperTest {

    @Test
    @DisplayName("StorePriceVerification → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        StorePriceVerification storePriceVerification = StorePriceVerification.reconstitute(
            231L,
            ShopId.of(232L),
            UploadedFileId.of(233L),
            StorePriceVerificationStatus.IN_PROGRESS,
            "거절",
            234L,
            LocalDateTime.of(2026, 2, 24, 10, 24),
            LocalDateTime.of(2026, 2, 25, 10, 25),
            LocalDateTime.of(2026, 2, 26, 10, 26)
        );

        StorePriceVerificationJpaEntity entity = StorePriceVerificationMapper.toEntity(storePriceVerification);

        assertThat(entity.getShopId()).isEqualTo(232L);
        assertThat(entity.getPriceListFileId()).isEqualTo(233L);
        assertThat(entity.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(entity.getRejectReason()).isEqualTo("거절");
        assertThat(entity.getRequestedByCeoId()).isEqualTo(234L);
        assertThat(entity.getProcessedAt()).isEqualTo(LocalDateTime.of(2026, 2, 24, 10, 24));
    }

    @Test
    @DisplayName("엔티티 → StorePriceVerification 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        StorePriceVerificationJpaEntity entity = StorePriceVerificationJpaEntity.create(
            232L,
            233L,
            "IN_PROGRESS",
            "거절",
            234L,
            LocalDateTime.of(2026, 2, 24, 10, 24)
        );
        ReflectionTestUtils.setField(entity, "id", 231L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 25, 10, 25));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 26, 10, 26));

        StorePriceVerification restored = StorePriceVerificationMapper.toDomain(entity);

        StorePriceVerification expected = StorePriceVerification.reconstitute(
            231L,
            ShopId.of(232L),
            UploadedFileId.of(233L),
            StorePriceVerificationStatus.IN_PROGRESS,
            "거절",
            234L,
            LocalDateTime.of(2026, 2, 24, 10, 24),
            LocalDateTime.of(2026, 2, 25, 10, 25),
            LocalDateTime.of(2026, 2, 26, 10, 26)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
