package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {
    List<ProductImageJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);
}
