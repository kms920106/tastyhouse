package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopAmenityCategoryState;
import com.tastyhouse.application.shop.port.out.write.ShopAmenityState;
import com.tastyhouse.application.shop.port.out.write.ShopBannerImageState;
import com.tastyhouse.application.shop.port.out.write.ShopBreakTimeState;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourState;
import com.tastyhouse.application.shop.port.out.write.ShopClosedDayState;
import com.tastyhouse.application.shop.port.out.write.ShopDetailStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeCategoryState;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeState;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodState;
import com.tastyhouse.application.shop.port.out.write.ShopOwnerMessageHistoryState;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryImageState;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryState;

import static com.tastyhouse.infrastructure.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;

@Repository
public class ShopDetailStatePortImpl implements ShopDetailStatePort {
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

    public ShopDetailStatePortImpl(JPAQueryFactory queryFactory, ShopBusinessHourJpaRepository shopBusinessHourJpaRepository, ShopBreakTimeJpaRepository shopBreakTimeJpaRepository, ShopClosedDayJpaRepository shopClosedDayJpaRepository, ShopAmenityCategoryJpaRepository shopAmenityCategoryJpaRepository, ShopFoodTypeCategoryJpaRepository shopFoodTypeCategoryJpaRepository, ShopAmenityJpaRepository shopAmenityJpaRepository, ShopFoodTypeJpaRepository shopFoodTypeJpaRepository, ShopOrderMethodJpaRepository shopOrderMethodJpaRepository, ShopBannerImageJpaRepository shopBannerImageJpaRepository, ShopPhotoCategoryJpaRepository shopPhotoCategoryJpaRepository, ShopPhotoCategoryImageJpaRepository shopPhotoCategoryImageJpaRepository, ShopOwnerMessageHistoryJpaRepository shopOwnerMessageHistoryJpaRepository) {
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
    public List<ShopBusinessHourState> findBusinessHoursByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBusinessHourJpaEntity)
            .where(shopBusinessHourJpaEntity.shopId.eq(shopId))
            .orderBy(shopBusinessHourJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBusinessHourMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopBusinessHourState> findBusinessHourById(Long id) {
        return shopBusinessHourJpaRepository.findById(id).map(ShopBusinessHourMapper::toState);
    }

    @Override
    public ShopBusinessHourState saveBusinessHour(ShopBusinessHourState businessHour) {
        if (businessHour.id() == null) {
            ShopBusinessHourJpaEntity saved = shopBusinessHourJpaRepository.save(ShopBusinessHourMapper.toEntity(businessHour));
            return ShopBusinessHourMapper.toState(saved);
        }

        ShopBusinessHourJpaEntity entity = shopBusinessHourJpaRepository.findById(businessHour.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업시간입니다: " + businessHour.id()));
        ShopBusinessHourMapper.applyChanges(entity, businessHour);
        return ShopBusinessHourMapper.toState(entity);
    }

    @Override
    public void deleteBusinessHourById(Long id) {
        shopBusinessHourJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopBreakTimeState> findBreakTimesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBreakTimeJpaEntity)
            .where(shopBreakTimeJpaEntity.shopId.eq(shopId))
            .orderBy(shopBreakTimeJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBreakTimeMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopBreakTimeState> findBreakTimeById(Long id) {
        return shopBreakTimeJpaRepository.findById(id).map(ShopBreakTimeMapper::toState);
    }

    @Override
    public ShopBreakTimeState saveBreakTime(ShopBreakTimeState breakTime) {
        if (breakTime.id() == null) {
            ShopBreakTimeJpaEntity saved = shopBreakTimeJpaRepository.save(ShopBreakTimeMapper.toEntity(breakTime));
            return ShopBreakTimeMapper.toState(saved);
        }

        ShopBreakTimeJpaEntity entity = shopBreakTimeJpaRepository.findById(breakTime.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 브레이크타임입니다: " + breakTime.id()));
        ShopBreakTimeMapper.applyChanges(entity, breakTime);
        return ShopBreakTimeMapper.toState(entity);
    }

    @Override
    public void deleteBreakTimeById(Long id) {
        shopBreakTimeJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopClosedDayState> findClosedDaysByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopClosedDayMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopClosedDayState> findClosedDayById(Long id) {
        return shopClosedDayJpaRepository.findById(id)
            .map(ShopClosedDayMapper::toState);
    }

    @Override
    public ShopClosedDayState saveClosedDay(ShopClosedDayState closedDay) {
        if (closedDay.id() == null) {
            ShopClosedDayJpaEntity saved = shopClosedDayJpaRepository.save(ShopClosedDayMapper.toEntity(closedDay));
            return ShopClosedDayMapper.toState(saved);
        }

        ShopClosedDayJpaEntity entity = shopClosedDayJpaRepository.findById(closedDay.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정기 휴무입니다: " + closedDay.id()));
        return ShopClosedDayMapper.toState(entity);
    }

    @Override
    public void deleteClosedDayById(Long id) {
        shopClosedDayJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ShopAmenityCategoryState> findAmenityCategoryById(Long id) {
        return shopAmenityCategoryJpaRepository.findById(id).map(ShopAmenityCategoryMapper::toState);
    }

    @Override
    public ShopAmenityCategoryState saveAmenityCategory(ShopAmenityCategoryState amenityCategory) {
        if (amenityCategory.id() == null) {
            ShopAmenityCategoryJpaEntity saved = shopAmenityCategoryJpaRepository.save(ShopAmenityCategoryMapper.toEntity(amenityCategory));
            return ShopAmenityCategoryMapper.toState(saved);
        }

        ShopAmenityCategoryJpaEntity entity = shopAmenityCategoryJpaRepository.findById(amenityCategory.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 편의시설 카테고리입니다: " + amenityCategory.id()));
        ShopAmenityCategoryMapper.applyChanges(entity, amenityCategory);
        return ShopAmenityCategoryMapper.toState(entity);
    }

    @Override
    public Optional<ShopFoodTypeCategoryState> findFoodTypeCategoryById(Long id) {
        return shopFoodTypeCategoryJpaRepository.findById(id).map(ShopFoodTypeCategoryMapper::toState);
    }

    @Override
    public ShopFoodTypeCategoryState saveFoodTypeCategory(ShopFoodTypeCategoryState foodTypeCategory) {
        if (foodTypeCategory.id() == null) {
            ShopFoodTypeCategoryJpaEntity saved = shopFoodTypeCategoryJpaRepository.save(ShopFoodTypeCategoryMapper.toEntity(foodTypeCategory));
            return ShopFoodTypeCategoryMapper.toState(saved);
        }

        ShopFoodTypeCategoryJpaEntity entity = shopFoodTypeCategoryJpaRepository.findById(foodTypeCategory.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 음식 유형 카테고리입니다: " + foodTypeCategory.id()));
        ShopFoodTypeCategoryMapper.applyChanges(entity, foodTypeCategory);
        return ShopFoodTypeCategoryMapper.toState(entity);
    }

    @Override
    public ShopAmenityState saveAmenity(ShopAmenityState amenity) {
        if (amenity.id() == null) {
            ShopAmenityJpaEntity saved = shopAmenityJpaRepository.save(ShopAmenityMapper.toEntity(amenity));
            return ShopAmenityMapper.toState(saved);
        }

        ShopAmenityJpaEntity entity = shopAmenityJpaRepository.findById(amenity.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 편의시설 배정입니다: " + amenity.id()));
        return ShopAmenityMapper.toState(entity);
    }

    @Override
    public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
        shopAmenityJpaRepository.deleteByShopIdAndShopAmenityCategoryId(shopId, shopAmenityCategoryId);
    }

    @Override
    public ShopFoodTypeState saveFoodType(ShopFoodTypeState foodType) {
        if (foodType.id() == null) {
            ShopFoodTypeJpaEntity saved = shopFoodTypeJpaRepository.save(ShopFoodTypeMapper.toEntity(foodType));
            return ShopFoodTypeMapper.toState(saved);
        }

        ShopFoodTypeJpaEntity entity = shopFoodTypeJpaRepository.findById(foodType.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 음식 유형 배정입니다: " + foodType.id()));
        return ShopFoodTypeMapper.toState(entity);
    }

    @Override
    public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
        shopFoodTypeJpaRepository.deleteByShopIdAndShopFoodTypeCategoryId(shopId, shopFoodTypeCategoryId);
    }

    @Override
    public List<ShopOrderMethodState> findOrderMethodsByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(ShopOrderMethodMapper::toState)
            .toList();
    }

    @Override
    public ShopOrderMethodState saveOrderMethod(ShopOrderMethodState orderMethod) {
        if (orderMethod.id() == null) {
            ShopOrderMethodJpaEntity saved = shopOrderMethodJpaRepository.save(ShopOrderMethodMapper.toEntity(orderMethod));
            return ShopOrderMethodMapper.toState(saved);
        }

        ShopOrderMethodJpaEntity entity = shopOrderMethodJpaRepository.findById(orderMethod.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문방식 배정입니다: " + orderMethod.id()));
        return ShopOrderMethodMapper.toState(entity);
    }

    @Override
    public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, String orderMethod) {
        shopOrderMethodJpaRepository.deleteByShopIdAndOrderMethod(shopId, orderMethod);
    }

    @Override
    public ShopBannerImageState saveBannerImage(ShopBannerImageState bannerImage) {
        if (bannerImage.id() == null) {
            ShopBannerImageJpaEntity saved = shopBannerImageJpaRepository.save(ShopBannerImageMapper.toEntity(bannerImage));
            return ShopBannerImageMapper.toState(saved);
        }

        ShopBannerImageJpaEntity entity = shopBannerImageJpaRepository.findById(bannerImage.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너 이미지입니다: " + bannerImage.id()));
        return ShopBannerImageMapper.toState(entity);
    }

    @Override
    public void deleteBannerImageById(Long id) {
        shopBannerImageJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ShopPhotoCategoryState> findPhotoCategoryById(Long id) {
        return shopPhotoCategoryJpaRepository.findById(id).map(ShopPhotoCategoryMapper::toState);
    }

    @Override
    public ShopPhotoCategoryState savePhotoCategory(ShopPhotoCategoryState photoCategory) {
        if (photoCategory.id() == null) {
            ShopPhotoCategoryJpaEntity saved = shopPhotoCategoryJpaRepository.save(ShopPhotoCategoryMapper.toEntity(photoCategory));
            return ShopPhotoCategoryMapper.toState(saved);
        }

        ShopPhotoCategoryJpaEntity entity = shopPhotoCategoryJpaRepository.findById(photoCategory.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리입니다: " + photoCategory.id()));
        ShopPhotoCategoryMapper.applyChanges(entity, photoCategory);
        return ShopPhotoCategoryMapper.toState(entity);
    }

    @Override
    public void deletePhotoCategoryById(Long id) {
        shopPhotoCategoryJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ShopPhotoCategoryImageState> findPhotoCategoryImageById(Long id) {
        return shopPhotoCategoryImageJpaRepository.findById(id).map(ShopPhotoCategoryImageMapper::toState);
    }

    @Override
    public ShopPhotoCategoryImageState savePhotoCategoryImage(ShopPhotoCategoryImageState photoCategoryImage) {
        if (photoCategoryImage.id() == null) {
            ShopPhotoCategoryImageJpaEntity saved = shopPhotoCategoryImageJpaRepository.save(ShopPhotoCategoryImageMapper.toEntity(photoCategoryImage));
            return ShopPhotoCategoryImageMapper.toState(saved);
        }

        ShopPhotoCategoryImageJpaEntity entity = shopPhotoCategoryImageJpaRepository.findById(photoCategoryImage.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리 이미지입니다: " + photoCategoryImage.id()));
        ShopPhotoCategoryImageMapper.applyChanges(entity, photoCategoryImage);
        return ShopPhotoCategoryImageMapper.toState(entity);
    }

    @Override
    public void deletePhotoCategoryImageById(Long id) {
        shopPhotoCategoryImageJpaRepository.deleteById(id);
    }

    @Override
    public void saveOwnerMessage(ShopOwnerMessageHistoryState ownerMessageHistory) {
        shopOwnerMessageHistoryJpaRepository.save(
            ShopOwnerMessageHistoryMapper.toEntity(ownerMessageHistory)
        );
    }

    @Override
    public Optional<ShopOwnerMessageHistoryState> findLatestOwnerMessage(Long shopId) {
        return shopOwnerMessageHistoryJpaRepository.findFirstByShopIdOrderByIdDesc(shopId)
            .map(ShopOwnerMessageHistoryMapper::toState);
    }
}
