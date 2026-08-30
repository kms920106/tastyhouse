package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달지역 도형 write 어댑터.
 *
 * <p>가게당 1건이라 조회는 {@code shopId} 단건뿐이다. 저장은 detached merge가 아니라 load-copy-save로
 * 수행해 감사 필드({@code created_at})가 보존되게 한다.
 */
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
