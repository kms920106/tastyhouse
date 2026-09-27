package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestState;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestStatePort;

import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;

@Repository
public class ShopImageChangeRequestStatePortImpl implements ShopImageChangeRequestStatePort {
    private final JPAQueryFactory queryFactory;
    private final ShopImageChangeRequestJpaRepository shopImageChangeRequestJpaRepository;

    public ShopImageChangeRequestStatePortImpl(JPAQueryFactory queryFactory, ShopImageChangeRequestJpaRepository shopImageChangeRequestJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopImageChangeRequestJpaRepository = shopImageChangeRequestJpaRepository;
    }

    @Override
    public ShopImageChangeRequestState save(ShopImageChangeRequestState shopImageChangeRequest) {
        if (shopImageChangeRequest.id() == null) {
            ShopImageChangeRequestJpaEntity saved =
                shopImageChangeRequestJpaRepository.save(ShopImageChangeRequestMapper.toEntity(shopImageChangeRequest));
            return ShopImageChangeRequestMapper.toState(saved);
        }

        ShopImageChangeRequestJpaEntity entity = shopImageChangeRequestJpaRepository.findById(shopImageChangeRequest.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이미지 변경 요청입니다: " + shopImageChangeRequest.id()));
        ShopImageChangeRequestMapper.applyChanges(entity, shopImageChangeRequest);
        return ShopImageChangeRequestMapper.toState(entity);
    }

    @Override
    public Optional<ShopImageChangeRequestState> findById(Long id) {
        return shopImageChangeRequestJpaRepository.findById(id)
            .map(ShopImageChangeRequestMapper::toState);
    }

    @Override
    public boolean existsByShopIdAndImageTypeAndStatus(Long shopId, String imageType, String status) {
        Integer result = queryFactory
            .selectOne()
            .from(shopImageChangeRequestJpaEntity)
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                imageTypeEq(imageType),
                statusEq(status)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public boolean existsByShopIdAndStatus(Long shopId, String status) {
        Integer result = queryFactory
            .selectOne()
            .from(shopImageChangeRequestJpaEntity)
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                statusEq(status)
            )
            .fetchFirst();
        return result != null;
    }

    private BooleanExpression statusEq(String status) {
        return status != null ? shopImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression imageTypeEq(String imageType) {
        return imageType != null ? shopImageChangeRequestJpaEntity.imageType.eq(imageType) : null;
    }
}
