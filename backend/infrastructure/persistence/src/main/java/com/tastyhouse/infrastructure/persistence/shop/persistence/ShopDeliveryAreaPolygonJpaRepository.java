package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryAreaPolygonJpaRepository extends JpaRepository<ShopDeliveryAreaPolygonJpaEntity, Long> {

    Optional<ShopDeliveryAreaPolygonJpaEntity> findByShopId(Long shopId);

    void deleteByShopId(Long shopId);
}
