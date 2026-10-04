package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {

    List<ProductImageJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);
}
