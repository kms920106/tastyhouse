package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopFoodType;

@Repository
public interface ShopFoodTypeJpaRepository extends JpaRepository<ShopFoodType, Long> {

    void deleteByShopIdAndShopFoodTypeCategoryId(Long shopId, Long shopFoodTypeCategoryId);
}
