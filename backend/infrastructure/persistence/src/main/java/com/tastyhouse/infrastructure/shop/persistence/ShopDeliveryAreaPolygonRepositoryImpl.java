package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopDeliveryAreaPolygonRepositoryImpl implements ShopDeliveryAreaPolygonRepository {
    private final ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository;

    public ShopDeliveryAreaPolygonRepositoryImpl(ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository) {
        this.shopDeliveryAreaPolygonJpaRepository = shopDeliveryAreaPolygonJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId) {
        return shopDeliveryAreaPolygonJpaRepository.findByShopId(shopId.value())
            .map(ShopDeliveryAreaPolygonMapper::toDomain);
    }

    @Override
    public ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        if (shopDeliveryAreaPolygon.getId() == null) {
            ShopDeliveryAreaPolygonJpaEntity saved = shopDeliveryAreaPolygonJpaRepository
                .save(ShopDeliveryAreaPolygonMapper.toEntity(shopDeliveryAreaPolygon));
            return ShopDeliveryAreaPolygonMapper.toDomain(saved);
        }

        ShopDeliveryAreaPolygonJpaEntity managed = shopDeliveryAreaPolygonJpaRepository
            .findById(shopDeliveryAreaPolygon.getId())
            .orElseThrow(() -> new IllegalStateException(
                "저장 대상 배달지역 도형을 찾을 수 없습니다: " + shopDeliveryAreaPolygon.getId()
            ));
        ShopDeliveryAreaPolygonMapper.applyChanges(managed, shopDeliveryAreaPolygon);
        return ShopDeliveryAreaPolygonMapper.toDomain(managed);
    }

    @Override
    public void deleteByShopId(ShopId shopId) {
        shopDeliveryAreaPolygonJpaRepository.deleteByShopId(shopId.value());
    }
}
