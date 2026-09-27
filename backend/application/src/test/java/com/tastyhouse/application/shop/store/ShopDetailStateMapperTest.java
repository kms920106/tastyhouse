package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDetailStateMapperTest {

    @Test
    @DisplayName("ShopAmenityCategory → ShopAmenityCategoryState → ShopAmenityCategory 왕복 시 모든 필드가 보존된다")
    void shopAmenityCategoryRoundTrip() {
        ShopAmenityCategory original = ShopAmenityCategory.reconstitute(
            101L,
            Amenity.RESERVATION,
            "v3",
            UploadedFileId.of(104L),
            UploadedFileId.of(105L),
            6,
            true
        );

        ShopAmenityCategory restored = ShopAmenityCategoryStateMapper.toDomain(ShopAmenityCategoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopFoodTypeCategory → ShopFoodTypeCategoryState → ShopFoodTypeCategory 왕복 시 모든 필드가 보존된다")
    void shopFoodTypeCategoryRoundTrip() {
        ShopFoodTypeCategory original = ShopFoodTypeCategory.reconstitute(
            108L,
            FoodType.JAPANESE,
            "v10",
            UploadedFileId.of(111L),
            UploadedFileId.of(112L),
            13,
            true
        );

        ShopFoodTypeCategory restored = ShopFoodTypeCategoryStateMapper.toDomain(ShopFoodTypeCategoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopAmenity → ShopAmenityState → ShopAmenity 왕복 시 모든 필드가 보존된다")
    void shopAmenityRoundTrip() {
        ShopAmenity original = ShopAmenity.reconstitute(
            115L,
            ShopId.of(116L),
            ShopAmenityCategoryId.of(117L)
        );

        ShopAmenity restored = ShopAmenityStateMapper.toDomain(ShopAmenityStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopFoodType → ShopFoodTypeState → ShopFoodType 왕복 시 모든 필드가 보존된다")
    void shopFoodTypeRoundTrip() {
        ShopFoodType original = ShopFoodType.reconstitute(
            118L,
            ShopId.of(119L),
            ShopFoodTypeCategoryId.of(120L)
        );

        ShopFoodType restored = ShopFoodTypeStateMapper.toDomain(ShopFoodTypeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBusinessHour → ShopBusinessHourState → ShopBusinessHour 왕복 시 모든 필드가 보존된다")
    void shopBusinessHourRoundTrip() {
        ShopBusinessHour original = ShopBusinessHour.reconstitute(
            121L,
            ShopId.of(122L),
            DayType.WEEKDAY,
            LocalTime.of(0, 24),
            LocalTime.of(1, 25),
            true,
            false
        );

        ShopBusinessHour restored = ShopBusinessHourStateMapper.toDomain(ShopBusinessHourStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBreakTime → ShopBreakTimeState → ShopBreakTime 왕복 시 모든 필드가 보존된다")
    void shopBreakTimeRoundTrip() {
        ShopBreakTime original = ShopBreakTime.reconstitute(
            128L,
            ShopId.of(129L),
            DayType.FRIDAY,
            LocalTime.of(7, 31),
            LocalTime.of(8, 32)
        );

        ShopBreakTime restored = ShopBreakTimeStateMapper.toDomain(ShopBreakTimeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopClosedDay → ShopClosedDayState → ShopClosedDay 왕복 시 모든 필드가 보존된다")
    void shopClosedDayRoundTrip() {
        ShopClosedDay original = ShopClosedDay.reconstitute(
            133L,
            ShopId.of(134L),
            ClosedDayType.EVERY_MONTH_FOURTH_WEEK_SUNDAY
        );

        ShopClosedDay restored = ShopClosedDayStateMapper.toDomain(ShopClosedDayStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopOrderMethod → ShopOrderMethodState → ShopOrderMethod 왕복 시 모든 필드가 보존된다")
    void shopOrderMethodRoundTrip() {
        ShopOrderMethod original = ShopOrderMethod.reconstitute(
            136L,
            ShopId.of(137L),
            OrderMethod.DELIVERY
        );

        ShopOrderMethod restored = ShopOrderMethodStateMapper.toDomain(ShopOrderMethodStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBannerImage → ShopBannerImageState → ShopBannerImage 왕복 시 모든 필드가 보존된다")
    void shopBannerImageRoundTrip() {
        ShopBannerImage original = ShopBannerImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42
        );

        ShopBannerImage restored = ShopBannerImageStateMapper.toDomain(ShopBannerImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopPhotoCategory → ShopPhotoCategoryState → ShopPhotoCategory 왕복 시 모든 필드가 보존된다")
    void shopPhotoCategoryRoundTrip() {
        ShopPhotoCategory original = ShopPhotoCategory.reconstitute(
            143L,
            ShopId.of(144L),
            "v45"
        );

        ShopPhotoCategory restored = ShopPhotoCategoryStateMapper.toDomain(ShopPhotoCategoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopPhotoCategoryImage → ShopPhotoCategoryImageState → ShopPhotoCategoryImage 왕복 시 모든 필드가 보존된다")
    void shopPhotoCategoryImageRoundTrip() {
        ShopPhotoCategoryImage original = ShopPhotoCategoryImage.reconstitute(
            146L,
            ShopPhotoCategoryId.of(147L),
            UploadedFileId.of(148L),
            49,
            true
        );

        ShopPhotoCategoryImage restored = ShopPhotoCategoryImageStateMapper.toDomain(ShopPhotoCategoryImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopOwnerMessageHistory → ShopOwnerMessageHistoryState → ShopOwnerMessageHistory 왕복 시 모든 필드가 보존된다")
    void shopOwnerMessageHistoryRoundTrip() {
        ShopOwnerMessageHistory original = ShopOwnerMessageHistory.reconstitute(
            151L,
            ShopId.of(152L),
            "v53",
            LocalDateTime.of(2026, 1, 27, 10, 54)
        );

        ShopOwnerMessageHistory restored = ShopOwnerMessageHistoryStateMapper.toDomain(ShopOwnerMessageHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
