package com.tastyhouse.core.repository.place;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.file.QUploadedFile;
import com.tastyhouse.core.entity.place.PlaceAmenity;
import com.tastyhouse.core.entity.place.PlaceBreakTime;
import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import com.tastyhouse.core.entity.place.PlaceClosedDay;
import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import com.tastyhouse.core.entity.place.PlaceOwnerMessageHistory;
import com.tastyhouse.core.entity.place.PlacePhotoCategory;
import com.tastyhouse.core.entity.place.PlaceStation;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceBannerImageDto;
import com.tastyhouse.core.entity.place.dto.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlacePhotoCategoryImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.entity.file.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.place.QPlaceAmenity.placeAmenity;
import static com.tastyhouse.core.entity.place.QPlaceAmenityCategory.placeAmenityCategory;
import static com.tastyhouse.core.entity.place.QPlaceBannerImage.placeBannerImage;
import static com.tastyhouse.core.entity.place.QPlaceBreakTime.placeBreakTime;
import static com.tastyhouse.core.entity.place.QPlaceBusinessHour.placeBusinessHour;
import static com.tastyhouse.core.entity.place.QPlaceClosedDay.placeClosedDay;
import static com.tastyhouse.core.entity.place.QPlaceFoodTypeCategory.placeFoodTypeCategory;
import static com.tastyhouse.core.entity.place.QPlaceOrderMethod.placeOrderMethod;
import static com.tastyhouse.core.entity.place.QPlaceOwnerMessageHistory.placeOwnerMessageHistory;
import static com.tastyhouse.core.entity.place.QPlacePhotoCategory.placePhotoCategory;
import static com.tastyhouse.core.entity.place.QPlacePhotoCategoryImage.placePhotoCategoryImage;
import static com.tastyhouse.core.entity.place.QPlaceStation.placeStation;

@Repository
@RequiredArgsConstructor
public class PlaceDetailRepositoryImpl implements PlaceDetailRepository {

    private static final QUploadedFile activeFile = new QUploadedFile("activeFile");
    private static final QUploadedFile inactiveFile = new QUploadedFile("inactiveFile");

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PlaceStation> findAllStationsOrderByName() {
        return queryFactory
            .selectFrom(placeStation)
            .orderBy(placeStation.stationName.asc())
            .fetch();
    }

    @Override
    public List<PlaceFoodTypeCategoryDto> findAllActiveFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(PlaceFoodTypeCategoryDto.class,
                placeFoodTypeCategory.id,
                placeFoodTypeCategory.foodType,
                placeFoodTypeCategory.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                placeFoodTypeCategory.sort,
                placeFoodTypeCategory.isActive
            ))
            .from(placeFoodTypeCategory)
            .join(activeFile).on(activeFile.id.eq(placeFoodTypeCategory.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(placeFoodTypeCategory.inactiveImageFileId))
            .where(placeFoodTypeCategory.isActive.eq(true))
            .orderBy(placeFoodTypeCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<PlaceAmenityCategoryDto> findAllActiveAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(PlaceAmenityCategoryDto.class,
                placeAmenityCategory.id,
                placeAmenityCategory.amenity,
                placeAmenityCategory.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                placeAmenityCategory.sort,
                placeAmenityCategory.isActive
            ))
            .from(placeAmenityCategory)
            .join(activeFile).on(activeFile.id.eq(placeAmenityCategory.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(placeAmenityCategory.inactiveImageFileId))
            .where(placeAmenityCategory.isActive.eq(true))
            .orderBy(placeAmenityCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<PlaceBusinessHour> findBusinessHoursByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placeBusinessHour)
            .where(placeBusinessHour.placeId.eq(placeId))
            .orderBy(placeBusinessHour.dayType.asc())
            .fetch();
    }

    @Override
    public List<PlaceBreakTime> findBreakTimesByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placeBreakTime)
            .where(placeBreakTime.placeId.eq(placeId))
            .orderBy(placeBreakTime.dayType.asc())
            .fetch();
    }

    @Override
    public List<PlaceClosedDay> findClosedDaysByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placeClosedDay)
            .where(placeClosedDay.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceAmenity> findAmenitiesByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placeAmenity)
            .where(placeAmenity.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceAmenityWithCategoryDto> findAmenitiesWithCategoryByPlaceId(Long placeId) {
        return queryFactory
            .select(Projections.constructor(PlaceAmenityWithCategoryDto.class,
                placeAmenityCategory.amenity,
                placeAmenityCategory.displayName,
                activeFile.filePath
            ))
            .from(placeAmenity)
            .join(placeAmenityCategory).on(placeAmenityCategory.id.eq(placeAmenity.placeAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(placeAmenityCategory.activeImageFileId))
            .where(placeAmenity.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceOrderMethod> findOrderMethodsByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placeOrderMethod)
            .where(placeOrderMethod.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceBannerImageDto> findBannerImagesByPlaceId(Long placeId) {
        return queryFactory
            .select(Projections.constructor(PlaceBannerImageDto.class,
                placeBannerImage.id,
                uploadedFile.filePath,
                placeBannerImage.sort
            ))
            .from(placeBannerImage)
            .join(uploadedFile).on(uploadedFile.id.eq(placeBannerImage.imageFileId))
            .where(placeBannerImage.placeId.eq(placeId))
            .orderBy(placeBannerImage.sort.asc())
            .fetch();
    }

    @Override
    public List<PlacePhotoCategory> findPhotoCategoriesByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(placePhotoCategory)
            .where(placePhotoCategory.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlacePhotoCategoryImageDto> findAllPhotoCategoryImages() {
        return queryFactory
            .select(Projections.constructor(PlacePhotoCategoryImageDto.class,
                placePhotoCategoryImage.id,
                placePhotoCategoryImage.placePhotoCategoryId,
                uploadedFile.filePath,
                placePhotoCategoryImage.sort
            ))
            .from(placePhotoCategoryImage)
            .join(uploadedFile).on(uploadedFile.id.eq(placePhotoCategoryImage.imageFileId))
            .orderBy(placePhotoCategoryImage.sort.asc())
            .fetch();
    }

    @Override
    public Optional<PlaceOwnerMessageHistory> findLatestOwnerMessageByPlaceId(Long placeId) {
        PlaceOwnerMessageHistory result = queryFactory
            .selectFrom(placeOwnerMessageHistory)
            .where(placeOwnerMessageHistory.placeId.eq(placeId))
            .orderBy(placeOwnerMessageHistory.createdAt.desc())
            .fetchFirst();
        return Optional.ofNullable(result);
    }
}
