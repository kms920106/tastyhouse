package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopAmenityCategoryJpaRepository extends JpaRepository<ShopAmenityCategory, Long> {
}
