package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryJpaEntity, Long> {
}
