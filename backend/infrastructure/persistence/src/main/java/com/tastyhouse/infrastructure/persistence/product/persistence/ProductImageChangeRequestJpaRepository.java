package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductImageChangeRequestJpaRepository
    extends JpaRepository<ProductImageChangeRequestJpaEntity, Long> {
}
