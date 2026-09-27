package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopState;
import com.tastyhouse.application.shop.port.out.write.ShopStatePort;

import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ShopStatePortImpl implements ShopStatePort {
    private final JPAQueryFactory queryFactory;
    private final ShopJpaRepository shopJpaRepository;

    public ShopStatePortImpl(JPAQueryFactory queryFactory, ShopJpaRepository shopJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopJpaRepository = shopJpaRepository;
    }

    @Override
    public Optional<ShopState> findById(Long id) {
        return shopJpaRepository.findById(id).map(ShopMapper::toState);
    }

    @Override
    public Optional<ShopState> findVisibleById(Long id) {
        ShopJpaEntity entity = queryFactory.selectFrom(shopJpaEntity)
            .where(
                shopJpaEntity.id.eq(id),
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopMapper::toState);
    }

    @Override
    public ShopState save(ShopState shop) {
        if (shop.id() == null) {
            ShopJpaEntity saved = shopJpaRepository.save(ShopMapper.toEntity(shop));
            return ShopMapper.toState(saved);
        }

        ShopJpaEntity entity = shopJpaRepository.findById(shop.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상점입니다: " + shop.id()));
        ShopMapper.applyChanges(entity, shop);
        return ShopMapper.toState(entity);
    }
}
