package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductBbqJpaRepository extends JpaRepository<ProductBbqJpaEntity, Long> {
}
