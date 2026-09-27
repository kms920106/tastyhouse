package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

class ShopDeliveryTipStateMapperTest {

    @Test
    @DisplayName("ShopDeliveryTipSetting → ShopDeliveryTipSettingState → ShopDeliveryTipSetting 왕복 시 모든 필드가 보존된다")
    void shopDeliveryTipSettingRoundTrip() {
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

        ShopDeliveryTipSetting restored = ShopDeliveryTipSettingStateMapper.toDomain(ShopDeliveryTipSettingStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipTier → ShopDeliveryTipTierState → ShopDeliveryTipTier 왕복 시 모든 필드가 보존된다")
    void shopDeliveryTipTierRoundTrip() {
        ShopDeliveryTipTier original = ShopDeliveryTipTier.reconstitute(
            109L,
            ShopId.of(110L),
            11,
            12,
            13
        );

        ShopDeliveryTipTier restored = ShopDeliveryTipTierStateMapper.toDomain(ShopDeliveryTipTierStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipRegion → ShopDeliveryTipRegionState → ShopDeliveryTipRegion 왕복 시 모든 필드가 보존된다")
    void shopDeliveryTipRegionRoundTrip() {
        ShopDeliveryTipRegion original = ShopDeliveryTipRegion.reconstitute(
            114L,
            ShopId.of(115L),
            AdminDongId.of(116L),
            17
        );

        ShopDeliveryTipRegion restored = ShopDeliveryTipRegionStateMapper.toDomain(ShopDeliveryTipRegionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipSchedule → ShopDeliveryTipScheduleState → ShopDeliveryTipSchedule 왕복 시 모든 필드가 보존된다")
    void shopDeliveryTipScheduleRoundTrip() {
        ShopDeliveryTipSchedule original = ShopDeliveryTipSchedule.reconstitute(
            118L,
            ShopId.of(119L),
            DayType.SATURDAY,
            LocalTime.of(21, 21),
            LocalTime.of(22, 22),
            23
        );

        ShopDeliveryTipSchedule restored = ShopDeliveryTipScheduleStateMapper.toDomain(ShopDeliveryTipScheduleStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopDeliveryTipHoliday → ShopDeliveryTipHolidayState → ShopDeliveryTipHoliday 왕복 시 모든 필드가 보존된다")
    void shopDeliveryTipHolidayRoundTrip() {
        ShopDeliveryTipHoliday original = ShopDeliveryTipHoliday.reconstitute(
            124L,
            ShopId.of(125L),
            26
        );

        ShopDeliveryTipHoliday restored = ShopDeliveryTipHolidayStateMapper.toDomain(ShopDeliveryTipHolidayStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
