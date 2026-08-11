package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.OrderMethod;
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
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;

/**
 * 가게 자식 애그리거트 write 어댑터.
 *
 * <p>표현 목적 read(카테고리 목록·배정 목록·배너·사진 목록, 주문방식 배정·사진 카테고리·최신 사장님
 * 한마디)는 같은 모듈의 {@link com.tastyhouse.infrastructure.shop.query.ShopQueryDao}로 이관했다
 * (공통 지침 패턴 4). 불변식 검증·영업 상태 판정에 쓰이는 목록 조회(영업시간·휴게시간·정기휴무)는
 * 도메인 소비자가 있어 write 어댑터에 남는다.
 */
@Repository
public class ShopDetailRepositoryImpl implements ShopDetailRepository {

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
    private final ShopOwnerMessageHistoryJpaRepository shopOwnerMessageHistoryJpaRepository;

    public ShopDetailRepositoryImpl(JPAQueryFactory queryFactory, ShopBusinessHourJpaRepository shopBusinessHourJpaRepository, ShopBreakTimeJpaRepository shopBreakTimeJpaRepository, ShopClosedDayJpaRepository shopClosedDayJpaRepository, ShopAmenityCategoryJpaRepository shopAmenityCategoryJpaRepository, ShopFoodTypeCategoryJpaRepository shopFoodTypeCategoryJpaRepository, ShopAmenityJpaRepository shopAmenityJpaRepository, ShopFoodTypeJpaRepository shopFoodTypeJpaRepository, ShopOrderMethodJpaRepository shopOrderMethodJpaRepository, ShopBannerImageJpaRepository shopBannerImageJpaRepository, ShopPhotoCategoryJpaRepository shopPhotoCategoryJpaRepository, ShopPhotoCategoryImageJpaRepository shopPhotoCategoryImageJpaRepository, ShopOwnerMessageHistoryJpaRepository shopOwnerMessageHistoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopBusinessHourJpaRepository = shopBusinessHourJpaRepository;
        this.shopBreakTimeJpaRepository = shopBreakTimeJpaRepository;
        this.shopClosedDayJpaRepository = shopClosedDayJpaRepository;
        this.shopAmenityCategoryJpaRepository = shopAmenityCategoryJpaRepository;
        this.shopFoodTypeCategoryJpaRepository = shopFoodTypeCategoryJpaRepository;
        this.shopAmenityJpaRepository = shopAmenityJpaRepository;
        this.shopFoodTypeJpaRepository = shopFoodTypeJpaRepository;
        this.shopOrderMethodJpaRepository = shopOrderMethodJpaRepository;
        this.shopBannerImageJpaRepository = shopBannerImageJpaRepository;
        this.shopPhotoCategoryJpaRepository = shopPhotoCategoryJpaRepository;
        this.shopPhotoCategoryImageJpaRepository = shopPhotoCategoryImageJpaRepository;
        this.shopOwnerMessageHistoryJpaRepository = shopOwnerMessageHistoryJpaRepository;
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
    public Optional<ShopClosedDay> findClosedDayById(Long id) {
        return shopClosedDayJpaRepository.findById(id)
            .map(ShopClosedDayMapper::toDomain);
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
    public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
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
    public void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory) {
        shopOwnerMessageHistoryJpaRepository.save(
            ShopOwnerMessageHistoryMapper.toEntity(ownerMessageHistory)
        );
    }

    @Override
    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
        return shopOwnerMessageHistoryJpaRepository.findFirstByShopIdOrderByIdDesc(shopId)
            .map(ShopOwnerMessageHistoryMapper::toDomain);
    }
}
