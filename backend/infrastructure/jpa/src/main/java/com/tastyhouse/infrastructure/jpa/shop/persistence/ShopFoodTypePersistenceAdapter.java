package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;

@Repository
class ShopFoodTypePersistenceAdapter implements ShopFoodTypeLoadPort, ShopFoodTypeSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopFoodTypeCategoryJpaRepository shopFoodTypeCategoryJpaRepository;
    private final ShopFoodTypeJpaRepository shopFoodTypeJpaRepository;

    public ShopFoodTypePersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopFoodTypeCategoryJpaRepository shopFoodTypeCategoryJpaRepository,
        ShopFoodTypeJpaRepository shopFoodTypeJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopFoodTypeCategoryJpaRepository = shopFoodTypeCategoryJpaRepository;
        this.shopFoodTypeJpaRepository = shopFoodTypeJpaRepository;
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
    public ShopFoodType saveFoodType(ShopFoodType foodType) {
        if (foodType.getId() == null) {
            ShopFoodTypeJpaEntity saved = shopFoodTypeJpaRepository.save(ShopFoodTypeMapper.toEntity(foodType));
            return ShopFoodTypeMapper.toDomain(saved);
        }

        ShopFoodTypeJpaEntity entity = shopFoodTypeJpaRepository.findById(foodType.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 음식 유형 배정입니다: " + foodType.getId()));
        return ShopFoodTypeMapper.toDomain(entity);
    }

    @Override
    public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
        List<ShopFoodTypeJpaEntity> rows = queryFactory
            .selectFrom(shopFoodTypeJpaEntity)
            .where(
                shopFoodTypeJpaEntity.shopId.eq(shopId),
                shopFoodTypeJpaEntity.shopFoodTypeCategoryId.eq(shopFoodTypeCategoryId)
            )
            .fetch();
        shopFoodTypeJpaRepository.deleteAll(rows);
    }
}
