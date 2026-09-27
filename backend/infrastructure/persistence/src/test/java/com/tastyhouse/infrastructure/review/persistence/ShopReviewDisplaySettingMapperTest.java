package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopReviewDisplaySettingMapperTest {

    @Test
    @DisplayName("ShopReviewDisplaySetting → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ShopReviewDisplaySettingJpaEntity entity = ShopReviewDisplaySettingMapper.toEntity(setting());

        assertThat(entity.getShopId()).isEqualTo(192L);
        assertThat(entity.getSortType()).isEqualTo("OLDEST");
    }

    @Test
    @DisplayName("엔티티 → ShopReviewDisplaySetting 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ShopReviewDisplaySetting original = setting();
        ShopReviewDisplaySettingJpaEntity entity = ShopReviewDisplaySettingMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 7, 7, 16, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        ShopReviewDisplaySetting restored = ShopReviewDisplaySettingMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ShopReviewDisplaySetting setting() {
        return ShopReviewDisplaySetting.reconstitute(
            191L, ShopId.of(192L), ReviewSortType.OLDEST, LocalDateTime.of(2026, 7, 8, 16, 0));
    }
}
