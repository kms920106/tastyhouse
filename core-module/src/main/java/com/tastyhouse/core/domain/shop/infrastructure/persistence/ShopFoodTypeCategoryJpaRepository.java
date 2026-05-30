package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopFoodTypeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopFoodTypeCategoryJpaRepository extends JpaRepository<ShopFoodTypeCategory, Long> {
}
