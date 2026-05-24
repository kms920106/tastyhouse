package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionGroupJpaRepository extends JpaRepository<ProductOptionGroup, Long> {
}
