package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopSuspensionJpaEntity.shopSuspensionJpaEntity;

@Repository
class ShopSuspensionPersistenceAdapter implements ShopSuspensionPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ShopSuspensionJpaRepository shopSuspensionJpaRepository;

    public ShopSuspensionPersistenceAdapter(JPAQueryFactory queryFactory, ShopSuspensionJpaRepository shopSuspensionJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopSuspensionJpaRepository = shopSuspensionJpaRepository;
    }

    @Override
    public ShopSuspension save(ShopSuspension shopSuspension) {
        if (shopSuspension.getId() == null) {
            ShopSuspensionJpaEntity saved = shopSuspensionJpaRepository.save(ShopSuspensionMapper.toEntity(shopSuspension));
            return ShopSuspensionMapper.toDomain(saved);
        }

        ShopSuspensionJpaEntity entity = shopSuspensionJpaRepository.findById(shopSuspension.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업 임시중지입니다: " + shopSuspension.getId()));
        ShopSuspensionMapper.applyChanges(entity, shopSuspension);
        return ShopSuspensionMapper.toDomain(entity);
    }

    @Override
    public List<ShopSuspension> findByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopSuspensionJpaEntity)
            .where(shopSuspensionJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopSuspensionMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopSuspension> findById(Long id) {
        return shopSuspensionJpaRepository.findById(id).map(ShopSuspensionMapper::toDomain);
    }
}
