package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopChangeHistoryMapperTest {

    @Test
    @DisplayName("ShopChangeHistory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopChangeHistory() {
        ShopChangeHistory original = ShopChangeHistory.reconstitute(
            113L,
            ShopId.of(114L),
            ShopChangeCategory.OPERATION,
            ShopChangeType.DELIVERY_AREA_ADJUSTMENT,
            ShopChangeActionType.DELETE,
            ShopChangeActorType.CEO,
            119L,
            "v20",
            "v21",
            LocalDateTime.of(2026, 1, 23, 10, 22)
        );

        ShopChangeHistoryJpaEntity entity = ShopChangeHistoryMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(114L);
        assertThat(entity.getCategory()).isEqualTo("OPERATION");
        assertThat(entity.getChangeType()).isEqualTo("DELIVERY_AREA_ADJUSTMENT");
        assertThat(entity.getActionType()).isEqualTo("DELETE");
        assertThat(entity.getActorType()).isEqualTo("CEO");
        assertThat(entity.getActorId()).isEqualTo(119L);
        assertThat(entity.getPreviousValue()).isEqualTo("v20");
        assertThat(entity.getNewValue()).isEqualTo("v21");
    }

    @Test
    @DisplayName("ShopChangeHistory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopChangeHistory() {
        ShopChangeHistory original = ShopChangeHistory.reconstitute(
            113L,
            ShopId.of(114L),
            ShopChangeCategory.OPERATION,
            ShopChangeType.DELIVERY_AREA_ADJUSTMENT,
            ShopChangeActionType.DELETE,
            ShopChangeActorType.CEO,
            119L,
            "v20",
            "v21",
            LocalDateTime.of(2026, 1, 23, 10, 22)
        );

        ShopChangeHistoryJpaEntity entity = ShopChangeHistoryJpaEntity.create(
            114L,
            "OPERATION",
            "DELIVERY_AREA_ADJUSTMENT",
            "DELETE",
            "CEO",
            119L,
            "v20",
            "v21"
        );
        ReflectionTestUtils.setField(entity, "id", 113L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 23, 10, 22));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopChangeHistoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
