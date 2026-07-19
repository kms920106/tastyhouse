package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;

@Repository
public interface ShopAmenityJpaRepository extends JpaRepository<ShopAmenity, Long> {

    void deleteByShopIdAndShopAmenityCategoryId(Long shopId, Long shopAmenityCategoryId);
}
