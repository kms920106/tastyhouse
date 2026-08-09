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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopDeliveryAreaAdjustmentRequestJpaEntity entity = shopDeliveryAreaAdjustmentRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달지역 조정 신청입니다: " + request.getId()));
        ShopDeliveryAreaAdjustmentRequestMapper.applyChanges(entity, request);
        return ShopDeliveryAreaAdjustmentRequestMapper.toDomain(entity);
    }
}
