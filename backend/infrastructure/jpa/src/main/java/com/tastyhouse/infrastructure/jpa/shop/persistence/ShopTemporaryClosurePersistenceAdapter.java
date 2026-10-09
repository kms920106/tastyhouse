package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopTemporaryClosureJpaEntity.shopTemporaryClosureJpaEntity;

@Repository
class ShopTemporaryClosurePersistenceAdapter implements ShopTemporaryClosureLoadPort, ShopTemporaryClosureSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopTemporaryClosureJpaRepository shopTemporaryClosureJpaRepository;

    public ShopTemporaryClosurePersistenceAdapter(JPAQueryFactory queryFactory, ShopTemporaryClosureJpaRepository shopTemporaryClosureJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopTemporaryClosureJpaRepository = shopTemporaryClosureJpaRepository;
    }

    @Override
    public ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure) {
        ShopTemporaryClosureJpaEntity saved = shopTemporaryClosureJpaRepository.save(ShopTemporaryClosureMapper.toEntity(shopTemporaryClosure));
        return ShopTemporaryClosureMapper.toDomain(saved);
    }

    @Override
    public List<ShopTemporaryClosure> findByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopTemporaryClosureJpaEntity)
            .where(shopTemporaryClosureJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopTemporaryClosureMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopTemporaryClosure> findById(Long id) {
        return shopTemporaryClosureJpaRepository.findById(id).map(ShopTemporaryClosureMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopTemporaryClosureJpaRepository.deleteById(id);
    }
}
