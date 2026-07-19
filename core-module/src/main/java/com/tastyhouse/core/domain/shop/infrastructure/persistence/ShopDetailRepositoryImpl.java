package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.file.domain.model.QUploadedFile;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodType;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenity.shopAmenity;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenityCategory.shopAmenityCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBannerImage.shopBannerImage;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBreakTime.shopBreakTime;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBusinessHour.shopBusinessHour;
import static com.tastyhouse.core.domain.shop.domain.model.QShopClosedDay.shopClosedDay;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodType.shopFoodType;
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
    private final ShopBusinessHourJpaRepository shopBusinessHourJpaRepository;
    private final ShopBreakTimeJpaRepository shopBreakTimeJpaRepository;
    private final ShopClosedDayJpaRepository shopClosedDayJpaRepository;
    private final ShopAmenityCategoryJpaRepository shopAmenityCategoryJpaRepository;
    private final ShopFoodTypeCategoryJpaRepository shopFoodTypeCategoryJpaRepository;
    private final ShopAmenityJpaRepository shopAmenityJpaRepository;
    private final ShopFoodTypeJpaRepository shopFoodTypeJpaRepository;
    private final ShopOrderMethodJpaRepository shopOrderMethodJpaRepository;
    private final ShopBannerImageJpaRepository shopBannerImageJpaRepository;
    private final ShopPhotoCategoryJpaRepository shopPhotoCategoryJpaRepository;
    private final ShopPhotoCategoryImageJpaRepository shopPhotoCategoryImageJpaRepository;

    @Override
    public List<Station> findAllStationsOrderByName() {
        return queryFactory
            .selectFrom(station)
            .orderBy(station.stationName.asc())
            .fetch();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findAllActiveFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
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
    public List<ShopAmenityCategoryResult> findAllActiveAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
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
    public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
        return shopBusinessHourJpaRepository.findById(id);
    }

    @Override
    public ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour) {
        return shopBusinessHourJpaRepository.save(businessHour);
    }

    @Override
    public void deleteBusinessHourById(Long id) {
        shopBusinessHourJpaRepository.deleteById(id);
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
    public Optional<ShopBreakTime> findBreakTimeById(Long id) {
        return shopBreakTimeJpaRepository.findById(id);
    }

    @Override
    public ShopBreakTime saveBreakTime(ShopBreakTime breakTime) {
        return shopBreakTimeJpaRepository.save(breakTime);
    }

    @Override
    public void deleteBreakTimeById(Long id) {
        shopBreakTimeJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopClosedDay)
            .where(shopClosedDay.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
        return shopClosedDayJpaRepository.save(closedDay);
    }

    @Override
    public void deleteClosedDayById(Long id) {
        shopClosedDayJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopAmenityCategory> findAllAmenityCategories() {
        return queryFactory
            .selectFrom(shopAmenityCategory)
            .orderBy(shopAmenityCategory.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
        return shopAmenityCategoryJpaRepository.findById(id);
    }

    @Override
    public ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory) {
        return shopAmenityCategoryJpaRepository.save(amenityCategory);
    }

    @Override
    public List<ShopFoodTypeCategory> findAllFoodTypeCategories() {
        return queryFactory
            .selectFrom(shopFoodTypeCategory)
            .orderBy(shopFoodTypeCategory.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id) {
        return shopFoodTypeCategoryJpaRepository.findById(id);
    }

    @Override
    public ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory) {
        return shopFoodTypeCategoryJpaRepository.save(foodTypeCategory);
    }

    @Override
    public ShopAmenity saveAmenity(ShopAmenity amenity) {
        return shopAmenityJpaRepository.save(amenity);
    }

    @Override
    public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
        shopAmenityJpaRepository.deleteByShopIdAndShopAmenityCategoryId(shopId, shopAmenityCategoryId);
    }

    @Override
    public ShopFoodType saveFoodType(ShopFoodType foodType) {
        return shopFoodTypeJpaRepository.save(foodType);
    }

    @Override
    public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
        shopFoodTypeJpaRepository.deleteByShopIdAndShopFoodTypeCategoryId(shopId, shopFoodTypeCategoryId);
    }

    @Override
    public List<ShopAmenityWithCategoryResult> findAmenitiesWithCategoryByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryResult.class,
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
    public List<ShopAmenityAssignmentResult> findAmenityAssignmentsByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityAssignmentResult.class,
                shopAmenity.id,
                shopAmenity.shopAmenityCategoryId,
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
    public List<ShopFoodTypeAssignmentResult> findFoodTypeAssignmentsByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeAssignmentResult.class,
                shopFoodType.id,
                shopFoodType.shopFoodTypeCategoryId,
                shopFoodTypeCategory.foodType,
                shopFoodTypeCategory.displayName,
                activeFile.filePath
            ))
            .from(shopFoodType)
            .join(shopFoodTypeCategory).on(shopFoodTypeCategory.id.eq(shopFoodType.shopFoodTypeCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategory.activeImageFileId))
            .where(shopFoodType.shopId.eq(shopId))
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
    public ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod) {
        return shopOrderMethodJpaRepository.save(orderMethod);
    }

    @Override
    public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod) {
        shopOrderMethodJpaRepository.deleteByShopIdAndOrderMethod(shopId, orderMethod);
    }

    @Override
    public List<ShopBannerImageResult> findBannerImagesByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageResult.class,
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
    public List<ShopBannerImage> findBannerImageEntitiesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBannerImage)
            .where(shopBannerImage.shopId.eq(shopId))
            .orderBy(shopBannerImage.sort.asc())
            .fetch();
    }

    @Override
    public ShopBannerImage saveBannerImage(ShopBannerImage bannerImage) {
        return shopBannerImageJpaRepository.save(bannerImage);
    }

    @Override
    public void deleteBannerImageById(Long id) {
        shopBannerImageJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopPhotoCategory)
            .where(shopPhotoCategory.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public Optional<ShopPhotoCategory> findPhotoCategoryById(Long id) {
        return shopPhotoCategoryJpaRepository.findById(id);
    }

    @Override
    public ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory) {
        return shopPhotoCategoryJpaRepository.save(photoCategory);
    }

    @Override
    public void deletePhotoCategoryById(Long id) {
        shopPhotoCategoryJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategoryImage> findPhotoCategoryImagesByCategoryId(Long shopPhotoCategoryId) {
        return shopPhotoCategoryImageJpaRepository.findByShopPhotoCategoryId(shopPhotoCategoryId);
    }

    @Override
    public Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id) {
        return shopPhotoCategoryImageJpaRepository.findById(id);
    }

    @Override
    public ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage) {
        return shopPhotoCategoryImageJpaRepository.save(photoCategoryImage);
    }

    @Override
    public void deletePhotoCategoryImageById(Long id) {
        shopPhotoCategoryImageJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageResult.class,
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
