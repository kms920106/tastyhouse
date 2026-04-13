package com.tastyhouse.core.repository.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.*;
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
    public List<PlaceFoodTypeCategory> findAllActiveFoodTypeCategories() {
        QPlaceFoodTypeCategory category = QPlaceFoodTypeCategory.placeFoodTypeCategory;
        return queryFactory
            .selectFrom(category)
            .where(category.isActive.eq(true))
            .orderBy(category.sort.asc())
            .fetch();
    }

    @Override
    public List<PlaceAmenityCategory> findAllActiveAmenityCategories() {
        QPlaceAmenityCategory category = QPlaceAmenityCategory.placeAmenityCategory;
        return queryFactory
            .selectFrom(category)
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
    public List<PlaceOrderMethod> findOrderMethodsByPlaceId(Long placeId) {
        QPlaceOrderMethod orderMethod = QPlaceOrderMethod.placeOrderMethod;
        return queryFactory
            .selectFrom(orderMethod)
            .where(orderMethod.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<PlaceBannerImage> findBannerImagesByPlaceId(Long placeId) {
        QPlaceBannerImage bannerImage = QPlaceBannerImage.placeBannerImage;
        return queryFactory
            .selectFrom(bannerImage)
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
    public List<PlacePhotoCategoryImage> findAllPhotoCategoryImages() {
        QPlacePhotoCategoryImage image = QPlacePhotoCategoryImage.placePhotoCategoryImage;
        return queryFactory
            .selectFrom(image)
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
