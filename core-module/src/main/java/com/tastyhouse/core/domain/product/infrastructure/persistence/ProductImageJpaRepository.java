package com.tastyhouse.core.domain.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, Long> {
}
