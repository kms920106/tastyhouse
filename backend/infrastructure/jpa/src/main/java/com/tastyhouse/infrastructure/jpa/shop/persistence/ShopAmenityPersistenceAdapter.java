package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.application.shop.port.out.write.ShopAmenityLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopAmenitySavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;

@Repository
class ShopAmenityPersistenceAdapter implements ShopAmenityLoadPort, ShopAmenitySavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopAmenityCategoryJpaRepository shopAmenityCategoryJpaRepository;
    private final ShopAmenityJpaRepository shopAmenityJpaRepository;

    public ShopAmenityPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopAmenityCategoryJpaRepository shopAmenityCategoryJpaRepository,
        ShopAmenityJpaRepository shopAmenityJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopAmenityCategoryJpaRepository = shopAmenityCategoryJpaRepository;
        this.shopAmenityJpaRepository = shopAmenityJpaRepository;
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
    public ShopAmenity saveAmenity(ShopAmenity amenity) {
        if (amenity.getId() == null) {
            ShopAmenityJpaEntity saved = shopAmenityJpaRepository.save(ShopAmenityMapper.toEntity(amenity));
            return ShopAmenityMapper.toDomain(saved);
        }

        ShopAmenityJpaEntity entity = shopAmenityJpaRepository.findById(amenity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 편의시설 배정입니다: " + amenity.getId()));
        return ShopAmenityMapper.toDomain(entity);
    }

    @Override
    public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
        List<ShopAmenityJpaEntity> rows = queryFactory
            .selectFrom(shopAmenityJpaEntity)
            .where(
                shopAmenityJpaEntity.shopId.eq(shopId),
                shopAmenityJpaEntity.shopAmenityCategoryId.eq(shopAmenityCategoryId)
            )
            .fetch();
        shopAmenityJpaRepository.deleteAll(rows);
    }
}
