package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ShopRepositoryImpl implements ShopRepository {
    private final JPAQueryFactory queryFactory;
    private final ShopJpaRepository shopJpaRepository;

    public ShopRepositoryImpl(JPAQueryFactory queryFactory, ShopJpaRepository shopJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopJpaRepository = shopJpaRepository;
    }

    @Override
    public Optional<Shop> findById(ShopId id) {
        return shopJpaRepository.findById(id.value()).map(ShopMapper::toDomain);
    }

    @Override
    public Optional<Shop> findVisibleById(ShopId id) {
        ShopJpaEntity entity = queryFactory.selectFrom(shopJpaEntity)
            .where(
                shopJpaEntity.id.eq(id.value()),
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopMapper::toDomain);
    }

    @Override
    public Shop save(Shop shop) {
        if (shop.getId() == null) {
            ShopJpaEntity saved = shopJpaRepository.save(ShopMapper.toEntity(shop));
            return ShopMapper.toDomain(saved);
        }

        ShopJpaEntity entity = shopJpaRepository.findById(shop.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상점입니다: " + shop.getId()));
        ShopMapper.applyChanges(entity, shop);
        return ShopMapper.toDomain(entity);
    }
}
