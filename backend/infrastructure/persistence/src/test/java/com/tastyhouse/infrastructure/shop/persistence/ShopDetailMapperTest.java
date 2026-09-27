package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

class ShopDetailMapperTest {

    @Test
    @DisplayName("ShopAmenityCategory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopAmenityCategory() {
        ShopAmenityCategory original = ShopAmenityCategory.reconstitute(
            101L,
            Amenity.RESERVATION,
            "v3",
            UploadedFileId.of(104L),
            UploadedFileId.of(105L),
            6,
            true
        );

        ShopAmenityCategoryJpaEntity entity = ShopAmenityCategoryMapper.toEntity(original);

        assertThat(entity.getAmenity()).isEqualTo("RESERVATION");
        assertThat(entity.getDisplayName()).isEqualTo("v3");
        assertThat(entity.getActiveImageFileId()).isEqualTo(104L);
        assertThat(entity.getInactiveImageFileId()).isEqualTo(105L);
        assertThat(entity.getSort()).isEqualTo(6);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("ShopAmenityCategory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopAmenityCategory() {
        ShopAmenityCategory original = ShopAmenityCategory.reconstitute(
            101L,
            Amenity.RESERVATION,
            "v3",
            UploadedFileId.of(104L),
            UploadedFileId.of(105L),
            6,
            true
        );

        ShopAmenityCategoryJpaEntity entity = ShopAmenityCategoryJpaEntity.create(
            "RESERVATION",
            "v3",
            104L,
            105L,
            6,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 101L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopAmenityCategoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopFoodTypeCategory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopFoodTypeCategory() {
        ShopFoodTypeCategory original = ShopFoodTypeCategory.reconstitute(
            108L,
            FoodType.JAPANESE,
            "v10",
            UploadedFileId.of(111L),
            UploadedFileId.of(112L),
            13,
            true
        );

        ShopFoodTypeCategoryJpaEntity entity = ShopFoodTypeCategoryMapper.toEntity(original);

        assertThat(entity.getFoodType()).isEqualTo("JAPANESE");
        assertThat(entity.getDisplayName()).isEqualTo("v10");
        assertThat(entity.getActiveImageFileId()).isEqualTo(111L);
        assertThat(entity.getInactiveImageFileId()).isEqualTo(112L);
        assertThat(entity.getSort()).isEqualTo(13);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("ShopFoodTypeCategory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopFoodTypeCategory() {
        ShopFoodTypeCategory original = ShopFoodTypeCategory.reconstitute(
            108L,
            FoodType.JAPANESE,
            "v10",
            UploadedFileId.of(111L),
            UploadedFileId.of(112L),
            13,
            true
        );

        ShopFoodTypeCategoryJpaEntity entity = ShopFoodTypeCategoryJpaEntity.create(
            "JAPANESE",
            "v10",
            111L,
            112L,
            13,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 108L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopFoodTypeCategoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopAmenity 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopAmenity() {
        ShopAmenity original = ShopAmenity.reconstitute(
            115L,
            ShopId.of(116L),
            ShopAmenityCategoryId.of(117L)
        );

        ShopAmenityJpaEntity entity = ShopAmenityMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(116L);
        assertThat(entity.getShopAmenityCategoryId()).isEqualTo(117L);
    }

    @Test
    @DisplayName("ShopAmenity 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopAmenity() {
        ShopAmenity original = ShopAmenity.reconstitute(
            115L,
            ShopId.of(116L),
            ShopAmenityCategoryId.of(117L)
        );

        ShopAmenityJpaEntity entity = ShopAmenityJpaEntity.create(
            116L,
            117L
        );
        ReflectionTestUtils.setField(entity, "id", 115L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopAmenityMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopFoodType 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopFoodType() {
        ShopFoodType original = ShopFoodType.reconstitute(
            118L,
            ShopId.of(119L),
            ShopFoodTypeCategoryId.of(120L)
        );

        ShopFoodTypeJpaEntity entity = ShopFoodTypeMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(119L);
        assertThat(entity.getShopFoodTypeCategoryId()).isEqualTo(120L);
    }

    @Test
    @DisplayName("ShopFoodType 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopFoodType() {
        ShopFoodType original = ShopFoodType.reconstitute(
            118L,
            ShopId.of(119L),
            ShopFoodTypeCategoryId.of(120L)
        );

        ShopFoodTypeJpaEntity entity = ShopFoodTypeJpaEntity.create(
            119L,
            120L
        );
        ReflectionTestUtils.setField(entity, "id", 118L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopFoodTypeMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBusinessHour 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopBusinessHour() {
        ShopBusinessHour original = ShopBusinessHour.reconstitute(
            121L,
            ShopId.of(122L),
            DayType.WEEKDAY,
            LocalTime.of(0, 24),
            LocalTime.of(1, 25),
            true,
            false
        );

        ShopBusinessHourJpaEntity entity = ShopBusinessHourMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(122L);
        assertThat(entity.getDayType()).isEqualTo("WEEKDAY");
        assertThat(entity.getOpenTime()).isEqualTo(LocalTime.of(0, 24));
        assertThat(entity.getCloseTime()).isEqualTo(LocalTime.of(1, 25));
        assertThat(entity.getIsClosed()).isEqualTo(true);
        assertThat(entity.getIs24Hours()).isEqualTo(false);
    }

    @Test
    @DisplayName("ShopBusinessHour 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopBusinessHour() {
        ShopBusinessHour original = ShopBusinessHour.reconstitute(
            121L,
            ShopId.of(122L),
            DayType.WEEKDAY,
            LocalTime.of(0, 24),
            LocalTime.of(1, 25),
            true,
            false
        );

        ShopBusinessHourJpaEntity entity = ShopBusinessHourJpaEntity.create(
            122L,
            "WEEKDAY",
            LocalTime.of(0, 24),
            LocalTime.of(1, 25),
            true,
            false
        );
        ReflectionTestUtils.setField(entity, "id", 121L);

        assertThat(ShopBusinessHourMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBreakTime 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopBreakTime() {
        ShopBreakTime original = ShopBreakTime.reconstitute(
            128L,
            ShopId.of(129L),
            DayType.FRIDAY,
            LocalTime.of(7, 31),
            LocalTime.of(8, 32)
        );

        ShopBreakTimeJpaEntity entity = ShopBreakTimeMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(129L);
        assertThat(entity.getDayType()).isEqualTo("FRIDAY");
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(7, 31));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(8, 32));
    }

    @Test
    @DisplayName("ShopBreakTime 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopBreakTime() {
        ShopBreakTime original = ShopBreakTime.reconstitute(
            128L,
            ShopId.of(129L),
            DayType.FRIDAY,
            LocalTime.of(7, 31),
            LocalTime.of(8, 32)
        );

        ShopBreakTimeJpaEntity entity = ShopBreakTimeJpaEntity.create(
            129L,
            "FRIDAY",
            LocalTime.of(7, 31),
            LocalTime.of(8, 32)
        );
        ReflectionTestUtils.setField(entity, "id", 128L);

        assertThat(ShopBreakTimeMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopClosedDay 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopClosedDay() {
        ShopClosedDay original = ShopClosedDay.reconstitute(
            133L,
            ShopId.of(134L),
            ClosedDayType.EVERY_MONTH_FOURTH_WEEK_SUNDAY
        );

        ShopClosedDayJpaEntity entity = ShopClosedDayMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(134L);
        assertThat(entity.getClosedDayType()).isEqualTo("EVERY_MONTH_FOURTH_WEEK_SUNDAY");
    }

    @Test
    @DisplayName("ShopClosedDay 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopClosedDay() {
        ShopClosedDay original = ShopClosedDay.reconstitute(
            133L,
            ShopId.of(134L),
            ClosedDayType.EVERY_MONTH_FOURTH_WEEK_SUNDAY
        );

        ShopClosedDayJpaEntity entity = ShopClosedDayJpaEntity.create(
            134L,
            "EVERY_MONTH_FOURTH_WEEK_SUNDAY"
        );
        ReflectionTestUtils.setField(entity, "id", 133L);

        assertThat(ShopClosedDayMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopOrderMethod 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopOrderMethod() {
        ShopOrderMethod original = ShopOrderMethod.reconstitute(
            136L,
            ShopId.of(137L),
            OrderMethod.DELIVERY
        );

        ShopOrderMethodJpaEntity entity = ShopOrderMethodMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(137L);
        assertThat(entity.getOrderMethod()).isEqualTo("DELIVERY");
    }

    @Test
    @DisplayName("ShopOrderMethod 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopOrderMethod() {
        ShopOrderMethod original = ShopOrderMethod.reconstitute(
            136L,
            ShopId.of(137L),
            OrderMethod.DELIVERY
        );

        ShopOrderMethodJpaEntity entity = ShopOrderMethodJpaEntity.create(
            137L,
            "DELIVERY"
        );
        ReflectionTestUtils.setField(entity, "id", 136L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopOrderMethodMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopBannerImage 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopBannerImage() {
        ShopBannerImage original = ShopBannerImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42
        );

        ShopBannerImageJpaEntity entity = ShopBannerImageMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(140L);
        assertThat(entity.getImageFileId()).isEqualTo(141L);
        assertThat(entity.getSort()).isEqualTo(42);
    }

    @Test
    @DisplayName("ShopBannerImage 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopBannerImage() {
        ShopBannerImage original = ShopBannerImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42
        );

        ShopBannerImageJpaEntity entity = ShopBannerImageJpaEntity.create(
            140L,
            141L,
            42
        );
        ReflectionTestUtils.setField(entity, "id", 139L);

        assertThat(ShopBannerImageMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopPhotoCategory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopPhotoCategory() {
        ShopPhotoCategory original = ShopPhotoCategory.reconstitute(
            143L,
            ShopId.of(144L),
            "v45"
        );

        ShopPhotoCategoryJpaEntity entity = ShopPhotoCategoryMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(144L);
        assertThat(entity.getName()).isEqualTo("v45");
    }

    @Test
    @DisplayName("ShopPhotoCategory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopPhotoCategory() {
        ShopPhotoCategory original = ShopPhotoCategory.reconstitute(
            143L,
            ShopId.of(144L),
            "v45"
        );

        ShopPhotoCategoryJpaEntity entity = ShopPhotoCategoryJpaEntity.create(
            144L,
            "v45"
        );
        ReflectionTestUtils.setField(entity, "id", 143L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopPhotoCategoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopPhotoCategoryImage 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopPhotoCategoryImage() {
        ShopPhotoCategoryImage original = ShopPhotoCategoryImage.reconstitute(
            146L,
            ShopPhotoCategoryId.of(147L),
            UploadedFileId.of(148L),
            49,
            true
        );

        ShopPhotoCategoryImageJpaEntity entity = ShopPhotoCategoryImageMapper.toEntity(original);

        assertThat(entity.getShopPhotoCategoryId()).isEqualTo(147L);
        assertThat(entity.getImageFileId()).isEqualTo(148L);
        assertThat(entity.getSort()).isEqualTo(49);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("ShopPhotoCategoryImage 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopPhotoCategoryImage() {
        ShopPhotoCategoryImage original = ShopPhotoCategoryImage.reconstitute(
            146L,
            ShopPhotoCategoryId.of(147L),
            UploadedFileId.of(148L),
            49,
            true
        );

        ShopPhotoCategoryImageJpaEntity entity = ShopPhotoCategoryImageJpaEntity.create(
            147L,
            148L,
            49,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 146L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopPhotoCategoryImageMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopOwnerMessageHistory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopOwnerMessageHistory() {
        ShopOwnerMessageHistory original = ShopOwnerMessageHistory.reconstitute(
            151L,
            ShopId.of(152L),
            "v53",
            LocalDateTime.of(2026, 1, 27, 10, 54)
        );

        ShopOwnerMessageHistoryJpaEntity entity = ShopOwnerMessageHistoryMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(152L);
        assertThat(entity.getMessage()).isEqualTo("v53");
    }

    @Test
    @DisplayName("ShopOwnerMessageHistory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopOwnerMessageHistory() {
        ShopOwnerMessageHistory original = ShopOwnerMessageHistory.reconstitute(
            151L,
            ShopId.of(152L),
            "v53",
            LocalDateTime.of(2026, 1, 27, 10, 54)
        );

        ShopOwnerMessageHistoryJpaEntity entity = ShopOwnerMessageHistoryJpaEntity.create(
            152L,
            "v53"
        );
        ReflectionTestUtils.setField(entity, "id", 151L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 27, 10, 54));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopOwnerMessageHistoryMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
