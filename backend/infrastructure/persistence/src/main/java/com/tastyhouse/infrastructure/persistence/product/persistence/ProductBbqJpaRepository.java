package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductBbqJpaRepository extends JpaRepository<ProductBbqJpaEntity, Long> {
}
