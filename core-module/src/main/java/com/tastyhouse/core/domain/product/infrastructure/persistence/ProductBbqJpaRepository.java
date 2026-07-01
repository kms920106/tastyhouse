package com.tastyhouse.core.domain.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.product.domain.model.ProductBbq;

public interface ProductBbqJpaRepository extends JpaRepository<ProductBbq, Long> {
}
