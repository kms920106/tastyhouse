package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopOrderNoticeJpaRepository extends JpaRepository<ShopOrderNoticeJpaEntity, Long> {

    Optional<ShopOrderNoticeJpaEntity> findByShopId(Long shopId);
}
