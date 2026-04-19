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
import com.tastyhouse.core.entity.place.QPlaceAmenity;
import com.tastyhouse.core.entity.place.QPlaceAmenityCategory;
import com.tastyhouse.core.entity.place.QPlaceBannerImage;
import com.tastyhouse.core.entity.place.QPlaceBreakTime;
import com.tastyhouse.core.entity.place.QPlaceBusinessHour;
import com.tastyhouse.core.entity.place.QPlaceClosedDay;
import com.tastyhouse.core.entity.place.QPlaceFoodTypeCategory;
import com.tastyhouse.core.entity.place.QPlaceOrderMethod;
import com.tastyhouse.core.entity.place.QPlaceOwnerMessageHistory;
import com.tastyhouse.core.entity.place.QPlacePhotoCategory;
import com.tastyhouse.core.entity.place.QPlacePhotoCategoryImage;
import com.tastyhouse.core.entity.place.QPlaceStation;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceBannerImageDto;
import com.tastyhouse.core.entity.place.dto.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlacePhotoCategoryImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaceDetailRepositoryImpl implements PlaceDetailRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PlaceStation> findAllStationsOrderByName() {
        QPlaceStation station = QPlaceStation.placeStation;
        return queryFactory
            .selectFrom(station)
            .orderBy(station.stationName.asc())
            .fetch();
    }

    @Override
    public List<PlaceFoodTypeCategoryDto> findAllActiveFoodTypeCategories() {
        QPlaceFoodTypeCategory category = QPlaceFoodTypeCategory.placeFoodTypeCategory;
        QUploadedFile activeFile = new QUploadedFile("activeFile");
        QUploadedFile inactiveFile = new QUploadedFile("inactiveFile");
        return queryFactory
            .select(Projections.constructor(PlaceFoodTypeCategoryDto.class,
                category.id,
                category.foodType,
                category.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                category.sort,
                category.isActive
            ))
            .from(category)
            .join(activeFile).on(activeFile.id.eq(category.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(category.inactiveImageFileId))
            .where(category.isActive.eq(true))
            .orderBy(category.sort.asc())
            .fetch();
    }

    @Override
    public List<PlaceAmenityCategoryDto> findAllActiveAmenityCategories() {
        QPlaceAmenityCategory category = QPlaceAmenityCategory.placeAmenityCategory;
        QUploadedFile activeFile = new QUploadedFile("activeFile");
        QUploadedFile inactiveFile = new QUploadedFile("inactiveFile");
        return queryFactory
            .select(Projections.constructor(PlaceAmenityCategoryDto.class,
                category.id,
                category.amenity,
                category.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                category.sort,
                category.isActive
            ))
            .from(category)
            .join(activeFile).on(activeFile.id.eq(category.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(category.inactiveImageFileId))
            .where(category.isActive.eq(true))
            .orderBy(category.sort.asc())
            .fetch();
    }

    @Override
    public List<PlaceBusinessHour> findBusinessHoursByPlaceId(Long placeId) {
        QPlaceBusinessHour businessHour = QPlaceBusinessHour.placeBusinessHour;
        return queryFactory
            .selectFrom(businessHour)
            .where(businessHour.placeId.eq(placeId))
            .orderBy(businessHour.dayType.asc())
            .fetch();
    }

    @Override
    public List<PlaceBreakTime> findBreakTimesByPlaceId(Long placeId) {
        QPlaceBreakTime breakTime = QPlaceBreakTime.placeBreakTime;
        return queryFactory
            .selectFrom(breakTime)
            .where(breakTime.placeId.eq(placeId))
            .orderBy(breakTime.dayType.asc())
            .fetch();
    }

    @Override
    public List<PlaceClosedDay> findClosedDaysByPlaceId(Long placeId) {
        QPlaceClosedDay closedDay = QPlaceClosedDay.placeClosedDay;
        return queryFactory
            .selectFrom(closedDay)
            .where(closedDay.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceAmenity> findAmenitiesByPlaceId(Long placeId) {
        QPlaceAmenity amenity = QPlaceAmenity.placeAmenity;
        return queryFactory
            .selectFrom(amenity)
            .where(amenity.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceAmenityWithCategoryDto> findAmenitiesWithCategoryByPlaceId(Long placeId) {
        QPlaceAmenity amenity = QPlaceAmenity.placeAmenity;
        QPlaceAmenityCategory category = QPlaceAmenityCategory.placeAmenityCategory;
        QUploadedFile activeFile = new QUploadedFile("activeFile");
        return queryFactory
            .select(Projections.constructor(PlaceAmenityWithCategoryDto.class,
                category.amenity,
                category.displayName,
                activeFile.filePath
            ))
            .from(amenity)
            .join(category).on(category.id.eq(amenity.placeAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(category.activeImageFileId))
            .where(amenity.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceOrderMethod> findOrderMethodsByPlaceId(Long placeId) {
        QPlaceOrderMethod orderMethod = QPlaceOrderMethod.placeOrderMethod;
        return queryFactory
            .selectFrom(orderMethod)
            .where(orderMethod.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceBannerImageDto> findBannerImagesByPlaceId(Long placeId) {
        QPlaceBannerImage bannerImage = QPlaceBannerImage.placeBannerImage;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;
        return queryFactory
            .select(Projections.constructor(PlaceBannerImageDto.class,
                bannerImage.id,
                uploadedFile.filePath,
                bannerImage.sort
            ))
            .from(bannerImage)
            .join(uploadedFile).on(uploadedFile.id.eq(bannerImage.imageFileId))
            .where(bannerImage.placeId.eq(placeId))
            .orderBy(bannerImage.sort.asc())
            .fetch();
    }

    @Override
    public List<PlacePhotoCategory> findPhotoCategoriesByPlaceId(Long placeId) {
        QPlacePhotoCategory category = QPlacePhotoCategory.placePhotoCategory;
        return queryFactory
            .selectFrom(category)
            .where(category.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlacePhotoCategoryImageDto> findAllPhotoCategoryImages() {
        QPlacePhotoCategoryImage image = QPlacePhotoCategoryImage.placePhotoCategoryImage;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;
        return queryFactory
            .select(Projections.constructor(PlacePhotoCategoryImageDto.class,
                image.id,
                image.placePhotoCategoryId,
                uploadedFile.filePath,
                image.sort
            ))
            .from(image)
            .join(uploadedFile).on(uploadedFile.id.eq(image.imageFileId))
            .orderBy(image.sort.asc())
            .fetch();
    }

    @Override
    public Optional<PlaceOwnerMessageHistory> findLatestOwnerMessageByPlaceId(Long placeId) {
        QPlaceOwnerMessageHistory history = QPlaceOwnerMessageHistory.placeOwnerMessageHistory;
        PlaceOwnerMessageHistory result = queryFactory
            .selectFrom(history)
            .where(history.placeId.eq(placeId))
            .orderBy(history.createdAt.desc())
            .fetchFirst();
        return Optional.ofNullable(result);
    }
}
