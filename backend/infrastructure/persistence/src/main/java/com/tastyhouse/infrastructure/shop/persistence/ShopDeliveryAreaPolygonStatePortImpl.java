package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonStatePort;

@Repository
public class ShopDeliveryAreaPolygonStatePortImpl implements ShopDeliveryAreaPolygonStatePort {
    private final ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository;

    public ShopDeliveryAreaPolygonStatePortImpl(ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository) {
        this.shopDeliveryAreaPolygonJpaRepository = shopDeliveryAreaPolygonJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaPolygonState> findByShopId(Long shopId) {
        return shopDeliveryAreaPolygonJpaRepository.findByShopId(shopId)
            .map(ShopDeliveryAreaPolygonMapper::toState);
    }

    @Override
    public ShopDeliveryAreaPolygonState save(ShopDeliveryAreaPolygonState shopDeliveryAreaPolygon) {
        if (shopDeliveryAreaPolygon.id() == null) {
            ShopDeliveryAreaPolygonJpaEntity saved = shopDeliveryAreaPolygonJpaRepository
                .save(ShopDeliveryAreaPolygonMapper.toEntity(shopDeliveryAreaPolygon));
            return ShopDeliveryAreaPolygonMapper.toState(saved);
        }

        ShopDeliveryAreaPolygonJpaEntity managed = shopDeliveryAreaPolygonJpaRepository
            .findById(shopDeliveryAreaPolygon.id())
            .orElseThrow(() -> new IllegalStateException(
                "저장 대상 배달지역 도형을 찾을 수 없습니다: " + shopDeliveryAreaPolygon.id()
            ));
        ShopDeliveryAreaPolygonMapper.applyChanges(managed, shopDeliveryAreaPolygon);
        return ShopDeliveryAreaPolygonMapper.toState(managed);
    }

    @Override
    public void deleteByShopId(Long shopId) {
        shopDeliveryAreaPolygonJpaRepository.deleteByShopId(shopId);
    }
}
