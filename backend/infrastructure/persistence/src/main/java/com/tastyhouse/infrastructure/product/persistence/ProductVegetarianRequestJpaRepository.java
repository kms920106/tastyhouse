package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVegetarianRequestJpaRepository
    extends JpaRepository<ProductVegetarianRequestJpaEntity, Long> {

    List<ProductVegetarianRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);
}
