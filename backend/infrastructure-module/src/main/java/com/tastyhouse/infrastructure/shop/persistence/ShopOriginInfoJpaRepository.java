package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOriginInfoJpaRepository extends JpaRepository<ShopOriginInfoJpaEntity, Long> {

    Optional<ShopOriginInfoJpaEntity> findByShopId(Long shopId);
}
