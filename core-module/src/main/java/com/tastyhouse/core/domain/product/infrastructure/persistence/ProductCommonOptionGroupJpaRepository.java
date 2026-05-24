package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommonOptionGroupJpaRepository extends JpaRepository<ProductCommonOptionGroup, Long> {
}
