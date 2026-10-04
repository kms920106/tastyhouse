package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageChangeRequestJpaRepository
    extends JpaRepository<ProductImageChangeRequestJpaEntity, Long> {

    List<ProductImageChangeRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);
}
