package com.tastyhouse.core.domain.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;

public interface ProductCommonOptionJpaRepository extends JpaRepository<ProductCommonOption, Long> {
}
