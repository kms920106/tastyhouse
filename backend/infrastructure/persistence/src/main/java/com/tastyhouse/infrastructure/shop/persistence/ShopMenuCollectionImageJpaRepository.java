package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuCollectionImageJpaRepository
    extends JpaRepository<ShopMenuCollectionImageJpaEntity, Long> {
    List<ShopMenuCollectionImageJpaEntity> findAllByShopIdOrderBySortAsc(Long shopId);
}
