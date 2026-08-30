package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRiderGuideJpaRepository extends JpaRepository<ShopRiderGuideJpaEntity, Long> {

    Optional<ShopRiderGuideJpaEntity> findByShopId(Long shopId);
}
