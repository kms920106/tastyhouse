package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopOriginInfoJpaRepository extends JpaRepository<ShopOriginInfoJpaEntity, Long> {

    Optional<ShopOriginInfoJpaEntity> findByShopId(Long shopId);
}
