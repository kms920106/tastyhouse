package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopFoodTypeJpaRepository extends JpaRepository<ShopFoodTypeJpaEntity, Long> {

    void deleteByShopIdAndShopFoodTypeCategoryId(ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId);
}
