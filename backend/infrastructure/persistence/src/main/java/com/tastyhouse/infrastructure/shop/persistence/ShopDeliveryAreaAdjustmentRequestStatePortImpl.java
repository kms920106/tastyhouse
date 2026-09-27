package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestStatePort;

@Repository
public class ShopDeliveryAreaAdjustmentRequestStatePortImpl implements ShopDeliveryAreaAdjustmentRequestStatePort {
    private final ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository;

    public ShopDeliveryAreaAdjustmentRequestStatePortImpl(ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository) {
        this.shopDeliveryAreaAdjustmentRequestJpaRepository = shopDeliveryAreaAdjustmentRequestJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaAdjustmentRequestState> findById(Long id) {
        return shopDeliveryAreaAdjustmentRequestJpaRepository.findById(id)
            .map(ShopDeliveryAreaAdjustmentRequestMapper::toState);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses) {
        return shopDeliveryAreaAdjustmentRequestJpaRepository.existsByShopIdAndStatusIn(shopId, statuses);
    }

    @Override
    public ShopDeliveryAreaAdjustmentRequestState save(ShopDeliveryAreaAdjustmentRequestState request) {
        if (request.id() == null) {
            ShopDeliveryAreaAdjustmentRequestJpaEntity saved = shopDeliveryAreaAdjustmentRequestJpaRepository
                .save(ShopDeliveryAreaAdjustmentRequestMapper.toEntity(request));
            return ShopDeliveryAreaAdjustmentRequestMapper.toState(saved);
        }

        ShopDeliveryAreaAdjustmentRequestJpaEntity entity = shopDeliveryAreaAdjustmentRequestJpaRepository.findById(request.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달지역 조정 신청입니다: " + request.id()));
        ShopDeliveryAreaAdjustmentRequestMapper.applyChanges(entity, request);
        return ShopDeliveryAreaAdjustmentRequestMapper.toState(entity);
    }
}
