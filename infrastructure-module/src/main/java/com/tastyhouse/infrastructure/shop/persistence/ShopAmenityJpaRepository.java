package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopAmenityJpaRepository extends JpaRepository<ShopAmenityJpaEntity, Long> {

    void deleteByShopIdAndShopAmenityCategoryId(ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId);
}
