package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopConvenienceInfoJpaRepository extends JpaRepository<ShopConvenienceInfoJpaEntity, Long> {

    Optional<ShopConvenienceInfoJpaEntity> findByShopId(Long shopId);
}
