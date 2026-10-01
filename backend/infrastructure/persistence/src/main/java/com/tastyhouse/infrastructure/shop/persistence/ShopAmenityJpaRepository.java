package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopAmenityJpaRepository extends JpaRepository<ShopAmenityJpaEntity, Long> {

    void deleteByShopIdAndShopAmenityCategoryId(Long shopId, Long shopAmenityCategoryId);
}
