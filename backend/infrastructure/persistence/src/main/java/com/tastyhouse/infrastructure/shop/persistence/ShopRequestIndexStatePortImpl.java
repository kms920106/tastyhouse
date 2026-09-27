package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexState;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexStatePort;

@Repository
public class ShopRequestIndexStatePortImpl implements ShopRequestIndexStatePort {
    private final ShopRequestIndexJpaRepository shopRequestIndexJpaRepository;

    public ShopRequestIndexStatePortImpl(ShopRequestIndexJpaRepository shopRequestIndexJpaRepository) {
        this.shopRequestIndexJpaRepository = shopRequestIndexJpaRepository;
    }

    @Override
    public ShopRequestIndexState save(ShopRequestIndexState shopRequestIndex) {
        if (shopRequestIndex.id() == null) {
            ShopRequestIndexJpaEntity saved =
                shopRequestIndexJpaRepository.save(ShopRequestIndexMapper.toEntity(shopRequestIndex));
            return ShopRequestIndexMapper.toState(saved);
        }

        ShopRequestIndexJpaEntity entity = shopRequestIndexJpaRepository.findById(shopRequestIndex.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 요청 인덱스입니다: " + shopRequestIndex.id()));
        ShopRequestIndexMapper.applyChanges(entity, shopRequestIndex);
        return ShopRequestIndexMapper.toState(entity);
    }

    @Override
    public Optional<ShopRequestIndexState> findById(Long id) {
        return shopRequestIndexJpaRepository.findById(id)
            .map(ShopRequestIndexMapper::toState);
    }

    @Override
    public Optional<ShopRequestIndexState> findByRequestTypeAndSourceRequestId(
        String requestType,
        Long sourceRequestId
    ) {
        return shopRequestIndexJpaRepository.findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .map(ShopRequestIndexMapper::toState);
    }
}
