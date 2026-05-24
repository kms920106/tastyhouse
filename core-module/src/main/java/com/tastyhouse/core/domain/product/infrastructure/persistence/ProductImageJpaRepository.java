package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, Long> {
}
