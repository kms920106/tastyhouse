package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {
}
