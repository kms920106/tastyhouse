package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {
}
