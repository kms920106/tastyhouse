package com.tastyhouse.core.domain.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, Long> {
}
