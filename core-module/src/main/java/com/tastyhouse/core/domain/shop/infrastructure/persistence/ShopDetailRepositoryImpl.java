package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.file.domain.model.QUploadedFile;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageDto;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenity.shopAmenity;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenityCategory.shopAmenityCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBannerImage.shopBannerImage;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBreakTime.shopBreakTime;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBusinessHour.shopBusinessHour;
import static com.tastyhouse.core.domain.shop.domain.model.QShopClosedDay.shopClosedDay;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodTypeCategory.shopFoodTypeCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopOrderMethod.shopOrderMethod;
import static com.tastyhouse.core.domain.shop.domain.model.QShopOwnerMessageHistory.shopOwnerMessageHistory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopPhotoCategory.shopPhotoCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopPhotoCategoryImage.shopPhotoCategoryImage;
import static com.tastyhouse.core.domain.shop.domain.model.QStation.station;

@Repository
@RequiredArgsConstructor
public class ShopDetailRepositoryImpl implements ShopDetailRepository {

    private static final QUploadedFile activeFile = new QUploadedFile("activeFile");
    private static final QUploadedFile inactiveFile = new QUploadedFile("inactiveFile");

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Station> findAllStationsOrderByName() {
        return queryFactory
            .selectFrom(station)
            .orderBy(station.stationName.asc())
            .fetch();
    }

    @Override
    public List<ShopFoodTypeCategoryDto> findAllActiveFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryDto.class,
                shopFoodTypeCategory.id,
                shopFoodTypeCategory.foodType,
                shopFoodTypeCategory.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategory.sort,
                shopFoodTypeCategory.visible
            ))
            .from(shopFoodTypeCategory)
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategory.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategory.inactiveImageFileId))
            .where(shopFoodTypeCategory.visible.eq(true))
            .orderBy(shopFoodTypeCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopAmenityCategoryDto> findAllActiveAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryDto.class,
                shopAmenityCategory.id,
                shopAmenityCategory.amenity,
                shopAmenityCategory.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategory.sort,
                shopAmenityCategory.visible
            ))
            .from(shopAmenityCategory)
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategory.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategory.inactiveImageFileId))
            .where(shopAmenityCategory.visible.eq(true))
            .orderBy(shopAmenityCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBusinessHour)
            .where(shopBusinessHour.shopId.eq(shopId))
            .orderBy(shopBusinessHour.dayType.asc())
            .fetch();
    }

    @Override
    public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBreakTime)
            .where(shopBreakTime.shopId.eq(shopId))
            .orderBy(shopBreakTime.dayType.asc())
            .fetch();
    }

    @Override
    public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopClosedDay)
            .where(shopClosedDay.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopAmenityWithCategoryDto> findAmenitiesWithCategoryByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryDto.class,
                shopAmenityCategory.amenity,
                shopAmenityCategory.displayName,
                activeFile.filePath
            ))
            .from(shopAmenity)
            .join(shopAmenityCategory).on(shopAmenityCategory.id.eq(shopAmenity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategory.activeImageFileId))
            .where(shopAmenity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopOrderMethod)
            .where(shopOrderMethod.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopBannerImageDto> findBannerImagesByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageDto.class,
                shopBannerImage.id,
                uploadedFile.filePath,
                shopBannerImage.sort
            ))
            .from(shopBannerImage)
            .join(uploadedFile).on(uploadedFile.id.eq(shopBannerImage.imageFileId))
            .where(shopBannerImage.shopId.eq(shopId))
            .orderBy(shopBannerImage.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopPhotoCategory)
            .where(shopPhotoCategory.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopPhotoCategoryImageDto> findAllPhotoCategoryImages() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageDto.class,
                shopPhotoCategoryImage.id,
                shopPhotoCategoryImage.shopPhotoCategoryId,
                uploadedFile.filePath,
                shopPhotoCategoryImage.sort
            ))
            .from(shopPhotoCategoryImage)
            .join(uploadedFile).on(uploadedFile.id.eq(shopPhotoCategoryImage.imageFileId))
            .orderBy(shopPhotoCategoryImage.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId) {
        ShopOwnerMessageHistory result = queryFactory
            .selectFrom(shopOwnerMessageHistory)
            .where(shopOwnerMessageHistory.shopId.eq(shopId))
            .orderBy(shopOwnerMessageHistory.createdAt.desc())
            .fetchFirst();
        return Optional.ofNullable(result);
    }
}
