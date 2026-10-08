package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductPriceJpaRepository extends JpaRepository<ProductPriceJpaEntity, Long> {
}
