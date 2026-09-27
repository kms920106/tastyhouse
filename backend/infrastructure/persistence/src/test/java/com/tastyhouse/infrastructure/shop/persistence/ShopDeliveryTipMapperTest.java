package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryTipMapperTest {

    @Test
    @DisplayName("ShopDeliveryTipSetting 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryTipSetting() {
        ShopDeliveryTipSetting original = ShopDeliveryTipSetting.reconstitute(
            101L,
            ShopId.of(102L),
            DeliveryTipExtraType.NONE,
            4,
            DeliveryTipDistanceUnit.PER_500M,
            6,
            LocalDateTime.of(2026, 1, 8, 10, 7),
            LocalDateTime.of(2026, 1, 9, 10, 8)
        );

        ShopDeliveryTipSettingJpaEntity entity = ShopDeliveryTipMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(102L);
        assertThat(entity.getExtraTipType()).isEqualTo("NONE");
        assertThat(entity.getBaseDistanceMeters()).isEqualTo(4);
        assertThat(entity.getSurchargeUnit()).isEqualTo("PER_500M");
        assertThat(entity.getSurchargeAmount()).isEqualTo(6);
    }

    @Test
    @DisplayName("ShopDeliveryTipSetting 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryTipSetting() {
        ShopDeliveryTipSetting original = ShopDeliveryTipSetting.reconstitute(
            101L,
            ShopId.of(102L),
            DeliveryTipExtraType.NONE,
            4,
            DeliveryTipDistanceUnit.PER_500M,
            6,
            LocalDateTime.of(2026, 1, 8, 10, 7),
            LocalDateTime.of(2026, 1, 9, 10, 8)
        );

        ShopDeliveryTipSettingJpaEntity entity = ShopDeliveryTipSettingJpaEntity.create(
            102L,
            "NONE",
            4,
            "PER_500M",
            6
        );
        ReflectionTestUtils.setField(entity, "id", 101L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 8, 10, 7));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 9, 10, 8));

        assertThat(ShopDeliveryTipMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipTier 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryTipTier() {
        ShopDeliveryTipTier original = ShopDeliveryTipTier.reconstitute(
            109L,
            ShopId.of(110L),
            11,
            12,
            13
        );

        ShopDeliveryTipTierJpaEntity entity = ShopDeliveryTipMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(110L);
        assertThat(entity.getTierOrder()).isEqualTo(11);
        assertThat(entity.getMinOrderAmount()).isEqualTo(12);
        assertThat(entity.getTipAmount()).isEqualTo(13);
    }

    @Test
    @DisplayName("ShopDeliveryTipTier 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryTipTier() {
        ShopDeliveryTipTier original = ShopDeliveryTipTier.reconstitute(
            109L,
            ShopId.of(110L),
            11,
            12,
            13
        );

        ShopDeliveryTipTierJpaEntity entity = ShopDeliveryTipTierJpaEntity.create(
            110L,
            11,
            12,
            13
        );
        ReflectionTestUtils.setField(entity, "id", 109L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryTipMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipRegion 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryTipRegion() {
        ShopDeliveryTipRegion original = ShopDeliveryTipRegion.reconstitute(
            114L,
            ShopId.of(115L),
            AdminDongId.of(116L),
            17
        );

        ShopDeliveryTipRegionJpaEntity entity = ShopDeliveryTipMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(115L);
        assertThat(entity.getAdminDongId()).isEqualTo(116L);
        assertThat(entity.getTipAmount()).isEqualTo(17);
    }

    @Test
    @DisplayName("ShopDeliveryTipRegion 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryTipRegion() {
        ShopDeliveryTipRegion original = ShopDeliveryTipRegion.reconstitute(
            114L,
            ShopId.of(115L),
            AdminDongId.of(116L),
            17
        );

        ShopDeliveryTipRegionJpaEntity entity = ShopDeliveryTipRegionJpaEntity.create(
            115L,
            116L,
            17
        );
        ReflectionTestUtils.setField(entity, "id", 114L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryTipMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipSchedule 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryTipSchedule() {
        ShopDeliveryTipSchedule original = ShopDeliveryTipSchedule.reconstitute(
            118L,
            ShopId.of(119L),
            DayType.SATURDAY,
            LocalTime.of(21, 21),
            LocalTime.of(22, 22),
            23
        );

        ShopDeliveryTipScheduleJpaEntity entity = ShopDeliveryTipMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(119L);
        assertThat(entity.getDayType()).isEqualTo("SATURDAY");
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(21, 21));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(22, 22));
        assertThat(entity.getTipAmount()).isEqualTo(23);
    }

    @Test
    @DisplayName("ShopDeliveryTipSchedule 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryTipSchedule() {
        ShopDeliveryTipSchedule original = ShopDeliveryTipSchedule.reconstitute(
            118L,
            ShopId.of(119L),
            DayType.SATURDAY,
            LocalTime.of(21, 21),
            LocalTime.of(22, 22),
            23
        );

        ShopDeliveryTipScheduleJpaEntity entity = ShopDeliveryTipScheduleJpaEntity.create(
            119L,
            "SATURDAY",
            LocalTime.of(21, 21),
            LocalTime.of(22, 22),
            23
        );
        ReflectionTestUtils.setField(entity, "id", 118L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryTipMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipHoliday 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryTipHoliday() {
        ShopDeliveryTipHoliday original = ShopDeliveryTipHoliday.reconstitute(
            124L,
            ShopId.of(125L),
            26
        );

        ShopDeliveryTipHolidayJpaEntity entity = ShopDeliveryTipMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(125L);
        assertThat(entity.getTipAmount()).isEqualTo(26);
    }

    @Test
    @DisplayName("ShopDeliveryTipHoliday 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryTipHoliday() {
        ShopDeliveryTipHoliday original = ShopDeliveryTipHoliday.reconstitute(
            124L,
            ShopId.of(125L),
            26
        );

        ShopDeliveryTipHolidayJpaEntity entity = ShopDeliveryTipHolidayJpaEntity.create(
            125L,
            26
        );
        ReflectionTestUtils.setField(entity, "id", 124L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryTipMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
