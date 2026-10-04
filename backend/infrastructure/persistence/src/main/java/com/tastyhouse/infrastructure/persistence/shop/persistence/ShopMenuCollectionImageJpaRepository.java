package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopMenuCollectionImageJpaRepository
    extends JpaRepository<ShopMenuCollectionImageJpaEntity, Long> {

    List<ShopMenuCollectionImageJpaEntity> findAllByShopIdOrderBySortAsc(Long shopId);
}
