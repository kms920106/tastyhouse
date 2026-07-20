package com.tastyhouse.infrastructure.shop.persistence;

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
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBannerImageJpaEntity.shopBannerImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOwnerMessageHistoryJpaEntity.shopOwnerMessageHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryImageJpaEntity.shopPhotoCategoryImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryJpaEntity.shopPhotoCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

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
            .selectFrom(stationJpaEntity)
            .orderBy(stationJpaEntity.stationName.asc())
            .fetch()
            .stream()
            .map(StationMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findAllActiveFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .where(shopFoodTypeCategoryJpaEntity.visible.eq(true))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopAmenityCategoryResult> findAllActiveAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .where(shopAmenityCategoryJpaEntity.visible.eq(true))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBusinessHourJpaEntity)
            .where(shopBusinessHourJpaEntity.shopId.eq(shopId))
            .orderBy(shopBusinessHourJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBusinessHourMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
        return shopBusinessHourJpaRepository.findById(id).map(ShopBusinessHourMapper::toDomain);
    }

    @Override
    public ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour) {
        if (businessHour.getId() == null) {
            ShopBusinessHourJpaEntity saved = shopBusinessHourJpaRepository.save(ShopBusinessHourMapper.toEntity(businessHour));
            return ShopBusinessHourMapper.toDomain(saved);
        }

        ShopBusinessHourJpaEntity entity = shopBusinessHourJpaRepository.findById(businessHour.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업시간입니다: " + businessHour.getId()));
        ShopBusinessHourMapper.applyChanges(entity, businessHour);
        return ShopBusinessHourMapper.toDomain(entity);
    }

    @Override
    public void deleteBusinessHourById(Long id) {
        shopBusinessHourJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBreakTimeJpaEntity)
            .where(shopBreakTimeJpaEntity.shopId.eq(shopId))
            .orderBy(shopBreakTimeJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBreakTimeMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBreakTime> findBreakTimeById(Long id) {
        return shopBreakTimeJpaRepository.findById(id).map(ShopBreakTimeMapper::toDomain);
    }

    @Override
    public ShopBreakTime saveBreakTime(ShopBreakTime breakTime) {
        if (breakTime.getId() == null) {
            ShopBreakTimeJpaEntity saved = shopBreakTimeJpaRepository.save(ShopBreakTimeMapper.toEntity(breakTime));
            return ShopBreakTimeMapper.toDomain(saved);
        }

        ShopBreakTimeJpaEntity entity = shopBreakTimeJpaRepository.findById(breakTime.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 브레이크타임입니다: " + breakTime.getId()));
        ShopBreakTimeMapper.applyChanges(entity, breakTime);
        return ShopBreakTimeMapper.toDomain(entity);
    }

    @Override
    public void deleteBreakTimeById(Long id) {
        shopBreakTimeJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopClosedDayMapper::toDomain)
            .toList();
    }

    @Override
    public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
        if (closedDay.getId() == null) {
            ShopClosedDayJpaEntity saved = shopClosedDayJpaRepository.save(ShopClosedDayMapper.toEntity(closedDay));
            return ShopClosedDayMapper.toDomain(saved);
        }

        // update 경로 없음(ShopClosedDay는 insert-only) — 존재 시에도 재조회만 수행
        ShopClosedDayJpaEntity entity = shopClosedDayJpaRepository.findById(closedDay.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정기 휴무입니다: " + closedDay.getId()));
        return ShopClosedDayMapper.toDomain(entity);
    }

    @Override
    public void deleteClosedDayById(Long id) {
        shopClosedDayJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopAmenityCategory> findAllAmenityCategories() {
        return queryFactory
            .selectFrom(shopAmenityCategoryJpaEntity)
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ShopAmenityCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
        return shopAmenityCategoryJpaRepository.findById(id).map(ShopAmenityCategoryMapper::toDomain);
    }

    @Override
    public ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory) {
        if (amenityCategory.getId() == null) {
            ShopAmenityCategoryJpaEntity saved = shopAmenityCategoryJpaRepository.save(ShopAmenityCategoryMapper.toEntity(amenityCategory));
            return ShopAmenityCategoryMapper.toDomain(saved);
        }

        ShopAmenityCategoryJpaEntity entity = shopAmenityCategoryJpaRepository.findById(amenityCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 편의시설 카테고리입니다: " + amenityCategory.getId()));
        ShopAmenityCategoryMapper.applyChanges(entity, amenityCategory);
        return ShopAmenityCategoryMapper.toDomain(entity);
    }

    @Override
    public List<ShopFoodTypeCategory> findAllFoodTypeCategories() {
        return queryFactory
            .selectFrom(shopFoodTypeCategoryJpaEntity)
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ShopFoodTypeCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id) {
        return shopFoodTypeCategoryJpaRepository.findById(id).map(ShopFoodTypeCategoryMapper::toDomain);
    }

    @Override
    public ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory) {
        if (foodTypeCategory.getId() == null) {
            ShopFoodTypeCategoryJpaEntity saved = shopFoodTypeCategoryJpaRepository.save(ShopFoodTypeCategoryMapper.toEntity(foodTypeCategory));
            return ShopFoodTypeCategoryMapper.toDomain(saved);
        }

        ShopFoodTypeCategoryJpaEntity entity = shopFoodTypeCategoryJpaRepository.findById(foodTypeCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 음식 유형 카테고리입니다: " + foodTypeCategory.getId()));
        ShopFoodTypeCategoryMapper.applyChanges(entity, foodTypeCategory);
        return ShopFoodTypeCategoryMapper.toDomain(entity);
    }

    @Override
    public ShopAmenity saveAmenity(ShopAmenity amenity) {
        if (amenity.getId() == null) {
            ShopAmenityJpaEntity saved = shopAmenityJpaRepository.save(ShopAmenityMapper.toEntity(amenity));
            return ShopAmenityMapper.toDomain(saved);
        }

        // update 경로 없음(ShopAmenity는 insert-only) — 존재 시에도 재조회만 수행
        ShopAmenityJpaEntity entity = shopAmenityJpaRepository.findById(amenity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 편의시설 배정입니다: " + amenity.getId()));
        return ShopAmenityMapper.toDomain(entity);
    }

    @Override
    public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
        shopAmenityJpaRepository.deleteByShopIdAndShopAmenityCategoryId(shopId, shopAmenityCategoryId);
    }

    @Override
    public ShopFoodType saveFoodType(ShopFoodType foodType) {
        if (foodType.getId() == null) {
            ShopFoodTypeJpaEntity saved = shopFoodTypeJpaRepository.save(ShopFoodTypeMapper.toEntity(foodType));
            return ShopFoodTypeMapper.toDomain(saved);
        }

        // update 경로 없음(ShopFoodType은 insert-only) — 존재 시에도 재조회만 수행
        ShopFoodTypeJpaEntity entity = shopFoodTypeJpaRepository.findById(foodType.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 음식 유형 배정입니다: " + foodType.getId()));
        return ShopFoodTypeMapper.toDomain(entity);
    }

    @Override
    public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
        shopFoodTypeJpaRepository.deleteByShopIdAndShopFoodTypeCategoryId(shopId, shopFoodTypeCategoryId);
    }

    @Override
    public List<ShopAmenityWithCategoryResult> findAmenitiesWithCategoryByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryResult.class,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopAmenityAssignmentResult> findAmenityAssignmentsByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityAssignmentResult.class,
                shopAmenityJpaEntity.id,
                shopAmenityJpaEntity.shopAmenityCategoryId,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> findFoodTypeAssignmentsByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeAssignmentResult.class,
                shopFoodTypeJpaEntity.id,
                shopFoodTypeJpaEntity.shopFoodTypeCategoryId,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .where(shopFoodTypeJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopOrderMethodMapper::toDomain)
            .toList();
    }

    @Override
    public ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod) {
        if (orderMethod.getId() == null) {
            ShopOrderMethodJpaEntity saved = shopOrderMethodJpaRepository.save(ShopOrderMethodMapper.toEntity(orderMethod));
            return ShopOrderMethodMapper.toDomain(saved);
        }

        // update 경로 없음(ShopOrderMethod는 insert-only) — 존재 시에도 재조회만 수행
        ShopOrderMethodJpaEntity entity = shopOrderMethodJpaRepository.findById(orderMethod.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문방식 배정입니다: " + orderMethod.getId()));
        return ShopOrderMethodMapper.toDomain(entity);
    }

    @Override
    public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod) {
        shopOrderMethodJpaRepository.deleteByShopIdAndOrderMethod(shopId, orderMethod);
    }

    @Override
    public List<ShopBannerImageResult> findBannerImagesByShopId(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageResult.class,
                shopBannerImageJpaEntity.id,
                uploadedFile.filePath,
                shopBannerImageJpaEntity.sort
            ))
            .from(shopBannerImageJpaEntity)
            .join(uploadedFile).on(uploadedFile.id.eq(shopBannerImageJpaEntity.imageFileId))
            .where(shopBannerImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopBannerImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopBannerImage> findBannerImageEntitiesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBannerImageJpaEntity)
            .where(shopBannerImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopBannerImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ShopBannerImageMapper::toDomain)
            .toList();
    }

    @Override
    public ShopBannerImage saveBannerImage(ShopBannerImage bannerImage) {
        if (bannerImage.getId() == null) {
            ShopBannerImageJpaEntity saved = shopBannerImageJpaRepository.save(ShopBannerImageMapper.toEntity(bannerImage));
            return ShopBannerImageMapper.toDomain(saved);
        }

        // update 경로 없음(ShopBannerImage는 insert-only) — 존재 시에도 재조회만 수행
        ShopBannerImageJpaEntity entity = shopBannerImageJpaRepository.findById(bannerImage.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너 이미지입니다: " + bannerImage.getId()));
        return ShopBannerImageMapper.toDomain(entity);
    }

    @Override
    public void deleteBannerImageById(Long id) {
        shopBannerImageJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopPhotoCategoryJpaEntity)
            .where(shopPhotoCategoryJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopPhotoCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopPhotoCategory> findPhotoCategoryById(Long id) {
        return shopPhotoCategoryJpaRepository.findById(id).map(ShopPhotoCategoryMapper::toDomain);
    }

    @Override
    public ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory) {
        if (photoCategory.getId() == null) {
            ShopPhotoCategoryJpaEntity saved = shopPhotoCategoryJpaRepository.save(ShopPhotoCategoryMapper.toEntity(photoCategory));
            return ShopPhotoCategoryMapper.toDomain(saved);
        }

        ShopPhotoCategoryJpaEntity entity = shopPhotoCategoryJpaRepository.findById(photoCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리입니다: " + photoCategory.getId()));
        ShopPhotoCategoryMapper.applyChanges(entity, photoCategory);
        return ShopPhotoCategoryMapper.toDomain(entity);
    }

    @Override
    public void deletePhotoCategoryById(Long id) {
        shopPhotoCategoryJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategoryImage> findPhotoCategoryImagesByCategoryId(Long shopPhotoCategoryId) {
        return shopPhotoCategoryImageJpaRepository.findByShopPhotoCategoryId(shopPhotoCategoryId)
            .stream()
            .map(ShopPhotoCategoryImageMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id) {
        return shopPhotoCategoryImageJpaRepository.findById(id).map(ShopPhotoCategoryImageMapper::toDomain);
    }

    @Override
    public ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage) {
        if (photoCategoryImage.getId() == null) {
            ShopPhotoCategoryImageJpaEntity saved = shopPhotoCategoryImageJpaRepository.save(ShopPhotoCategoryImageMapper.toEntity(photoCategoryImage));
            return ShopPhotoCategoryImageMapper.toDomain(saved);
        }

        ShopPhotoCategoryImageJpaEntity entity = shopPhotoCategoryImageJpaRepository.findById(photoCategoryImage.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리 이미지입니다: " + photoCategoryImage.getId()));
        ShopPhotoCategoryImageMapper.applyChanges(entity, photoCategoryImage);
        return ShopPhotoCategoryImageMapper.toDomain(entity);
    }

    @Override
    public void deletePhotoCategoryImageById(Long id) {
        shopPhotoCategoryImageJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                uploadedFile.filePath,
                shopPhotoCategoryImageJpaEntity.sort
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFile).on(uploadedFile.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId))
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId) {
        ShopOwnerMessageHistoryJpaEntity result = queryFactory
            .selectFrom(shopOwnerMessageHistoryJpaEntity)
            .where(shopOwnerMessageHistoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopOwnerMessageHistoryJpaEntity.createdAt.desc())
            .fetchFirst();
        return Optional.ofNullable(result).map(ShopOwnerMessageHistoryMapper::toDomain);
    }
}
