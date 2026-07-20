package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
}
