package com.tastyhouse.core.domain.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
}
