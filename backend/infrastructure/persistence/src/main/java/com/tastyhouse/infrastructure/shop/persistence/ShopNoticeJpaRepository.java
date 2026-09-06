package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopNoticeJpaRepository extends JpaRepository<ShopNoticeJpaEntity, Long> {
    Optional<ShopNoticeJpaEntity> findFirstByShopIdAndExposedIsTrueOrderByIdDesc(Long shopId);
}
