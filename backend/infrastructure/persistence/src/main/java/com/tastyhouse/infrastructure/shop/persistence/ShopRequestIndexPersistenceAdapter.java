package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexPersistencePort;

@Repository
public class ShopRequestIndexPersistenceAdapter implements ShopRequestIndexPersistencePort {
    private final ShopRequestIndexJpaRepository shopRequestIndexJpaRepository;

    public ShopRequestIndexPersistenceAdapter(ShopRequestIndexJpaRepository shopRequestIndexJpaRepository) {
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
        return shopRequestIndexJpaRepository.findByRequestTypeAndSourceRequestId(
            requestType == null ? null : requestType.name(),
            sourceRequestId
        )
            .map(ShopRequestIndexMapper::toDomain);
    }
}
