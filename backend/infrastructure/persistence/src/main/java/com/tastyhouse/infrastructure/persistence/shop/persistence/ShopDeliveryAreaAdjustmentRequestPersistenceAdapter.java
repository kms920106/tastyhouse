package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestPersistencePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopDeliveryAreaAdjustmentRequestJpaEntity.shopDeliveryAreaAdjustmentRequestJpaEntity;

@Repository
class ShopDeliveryAreaAdjustmentRequestPersistenceAdapter implements ShopDeliveryAreaAdjustmentRequestPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository;

    public ShopDeliveryAreaAdjustmentRequestPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopDeliveryAreaAdjustmentRequestJpaRepository = shopDeliveryAreaAdjustmentRequestJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id) {
        return shopDeliveryAreaAdjustmentRequestJpaRepository.findById(id)
            .map(ShopDeliveryAreaAdjustmentRequestMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses) {
        if (statuses.isEmpty()) {
            return false;
        }
        Integer found = queryFactory
            .selectOne()
            .from(shopDeliveryAreaAdjustmentRequestJpaEntity)
            .where(
                shopDeliveryAreaAdjustmentRequestJpaEntity.shopId.eq(shopId.value()),
                shopDeliveryAreaAdjustmentRequestJpaEntity.status.in(
                    statuses.stream().map(DeliveryAreaAdjustmentStatus::name).toList()
                )
            )
            .fetchFirst();
        return found != null;
    }

    @Override
    public ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request) {
        if (request.getId() == null) {
            ShopDeliveryAreaAdjustmentRequestJpaEntity saved = shopDeliveryAreaAdjustmentRequestJpaRepository
                .save(ShopDeliveryAreaAdjustmentRequestMapper.toEntity(request));
            return ShopDeliveryAreaAdjustmentRequestMapper.toDomain(saved);
        }

        ShopDeliveryAreaAdjustmentRequestJpaEntity entity = shopDeliveryAreaAdjustmentRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달지역 조정 신청입니다: " + request.getId()));
        ShopDeliveryAreaAdjustmentRequestMapper.applyChanges(entity, request);
        return ShopDeliveryAreaAdjustmentRequestMapper.toDomain(entity);
    }
}
