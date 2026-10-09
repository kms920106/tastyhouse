package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopRequestIndexJpaEntity.shopRequestIndexJpaEntity;

@Repository
class ShopRequestIndexPersistenceAdapter implements ShopRequestIndexLoadPort, ShopRequestIndexSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopRequestIndexJpaRepository shopRequestIndexJpaRepository;

    public ShopRequestIndexPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopRequestIndexJpaRepository shopRequestIndexJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopRequestIndexJpaRepository = shopRequestIndexJpaRepository;
    }

    @Override
    public ShopRequestIndex save(ShopRequestIndex shopRequestIndex) {
        if (shopRequestIndex.getId() == null) {
            ShopRequestIndexJpaEntity saved =
                shopRequestIndexJpaRepository.save(ShopRequestIndexMapper.toEntity(shopRequestIndex));
            return ShopRequestIndexMapper.toDomain(saved);
        }

        ShopRequestIndexJpaEntity entity = shopRequestIndexJpaRepository.findById(shopRequestIndex.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 요청 인덱스입니다: " + shopRequestIndex.getId()));
        ShopRequestIndexMapper.applyChanges(entity, shopRequestIndex);
        return ShopRequestIndexMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopRequestIndex> findById(Long id) {
        return shopRequestIndexJpaRepository.findById(id)
            .map(ShopRequestIndexMapper::toDomain);
    }

    @Override
    public Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(ShopRequestType requestType, Long sourceRequestId) {
        ShopRequestIndexJpaEntity entity = queryFactory
            .selectFrom(shopRequestIndexJpaEntity)
            .where(
                requestTypeEq(requestType == null ? null : requestType.name()),
                sourceRequestIdEq(sourceRequestId)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopRequestIndexMapper::toDomain);
    }

    private BooleanExpression requestTypeEq(String requestType) {
        return requestType == null
            ? shopRequestIndexJpaEntity.requestType.isNull()
            : shopRequestIndexJpaEntity.requestType.eq(requestType);
    }

    private BooleanExpression sourceRequestIdEq(Long sourceRequestId) {
        return sourceRequestId == null
            ? shopRequestIndexJpaEntity.sourceRequestId.isNull()
            : shopRequestIndexJpaEntity.sourceRequestId.eq(sourceRequestId);
    }
}
