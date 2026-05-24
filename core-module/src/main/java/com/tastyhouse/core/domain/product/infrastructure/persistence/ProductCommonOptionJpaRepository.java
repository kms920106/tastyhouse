package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommonOptionJpaRepository extends JpaRepository<ProductCommonOption, Long> {
}
