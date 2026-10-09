package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRiderGuideMapperTest {

    @Test
    @DisplayName("ShopRiderGuide 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopRiderGuide() {
        ShopRiderGuide original = ShopRiderGuide.reconstitute(
            147L,
            ShopId.of(148L),
            "v49",
            "v50",
            "v51",
            "v52",
            new BigDecimal("53.125"),
            new BigDecimal("54.125"),
            LocalDateTime.of(2026, 1, 28, 10, 55),
            LocalDateTime.of(2026, 1, 1, 10, 56)
        );

        ShopRiderGuideJpaEntity entity = ShopRiderGuideMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(148L);
        assertThat(entity.getVisitGuide()).isEqualTo("v49");
        assertThat(entity.getPickupRoadAddress()).isEqualTo("v50");
        assertThat(entity.getPickupLotAddress()).isEqualTo("v51");
        assertThat(entity.getPickupDetailAddress()).isEqualTo("v52");
        assertThat(entity.getPickupLatitude()).isEqualTo(new BigDecimal("53.125"));
        assertThat(entity.getPickupLongitude()).isEqualTo(new BigDecimal("54.125"));
    }

    @Test
    @DisplayName("ShopRiderGuide 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopRiderGuide() {
        ShopRiderGuide original = ShopRiderGuide.reconstitute(
            147L,
            ShopId.of(148L),
            "v49",
            "v50",
            "v51",
            "v52",
            new BigDecimal("53.125"),
            new BigDecimal("54.125"),
            LocalDateTime.of(2026, 1, 28, 10, 55),
            LocalDateTime.of(2026, 1, 1, 10, 56)
        );

        ShopRiderGuideJpaEntity entity = ShopRiderGuideJpaEntity.create(
            148L,
            "v49",
            "v50",
            "v51",
            "v52",
            new BigDecimal("53.125"),
            new BigDecimal("54.125")
        );
        ReflectionTestUtils.setField(entity, "id", 147L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 28, 10, 55));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 1, 10, 56));

        assertThat(ShopRiderGuideMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopRiderGuideHistory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopRiderGuideHistory() {
        ShopRiderGuideHistory original = ShopRiderGuideHistory.reconstitute(
            157L,
            ShopId.of(158L),
            RiderGuideActorType.ADMIN,
            160L,
            RiderGuideActionType.REVISION_REQUEST,
            "v62",
            "v63",
            "v64",
            LocalDateTime.of(2026, 1, 10, 10, 5)
        );

        ShopRiderGuideHistoryJpaEntity entity = ShopRiderGuideHistoryMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(158L);
        assertThat(entity.getActorType()).isEqualTo("ADMIN");
        assertThat(entity.getActorId()).isEqualTo(160L);
        assertThat(entity.getActionType()).isEqualTo("REVISION_REQUEST");
        assertThat(entity.getPreviousVisitGuide()).isEqualTo("v62");
        assertThat(entity.getNewVisitGuide()).isEqualTo("v63");
        assertThat(entity.getReason()).isEqualTo("v64");
    }

    @Test
    @DisplayName("ShopRiderGuideHistory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopRiderGuideHistory() {
        ShopRiderGuideHistory original = ShopRiderGuideHistory.reconstitute(
            157L,
            ShopId.of(158L),
            RiderGuideActorType.ADMIN,
            160L,
            RiderGuideActionType.REVISION_REQUEST,
            "v62",
            "v63",
            "v64",
            LocalDateTime.of(2026, 1, 10, 10, 5)
        );

        ShopRiderGuideHistoryJpaEntity entity = ShopRiderGuideHistoryJpaEntity.create(
            158L,
            "ADMIN",
            160L,
            "REVISION_REQUEST",
            "v62",
            "v63",
            "v64"
        );
        ReflectionTestUtils.setField(entity, "id", 157L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 10, 10, 5));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopRiderGuideHistoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
