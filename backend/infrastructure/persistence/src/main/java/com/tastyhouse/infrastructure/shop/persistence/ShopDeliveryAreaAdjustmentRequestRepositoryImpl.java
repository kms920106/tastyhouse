package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopDeliveryAreaAdjustmentRequestRepositoryImpl implements ShopDeliveryAreaAdjustmentRequestRepository {
    private final ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository;

    public ShopDeliveryAreaAdjustmentRequestRepositoryImpl(ShopDeliveryAreaAdjustmentRequestJpaRepository shopDeliveryAreaAdjustmentRequestJpaRepository) {
        this.shopDeliveryAreaAdjustmentRequestJpaRepository = shopDeliveryAreaAdjustmentRequestJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id) {
        return shopDeliveryAreaAdjustmentRequestJpaRepository.findById(id)
            .map(ShopDeliveryAreaAdjustmentRequestMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses) {
        return shopDeliveryAreaAdjustmentRequestJpaRepository.existsByShopIdAndStatusIn(shopId.value(), statuses);
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
