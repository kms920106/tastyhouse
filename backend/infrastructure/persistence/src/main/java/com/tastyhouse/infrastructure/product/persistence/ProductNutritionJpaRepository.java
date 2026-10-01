package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductNutritionJpaRepository extends JpaRepository<ProductNutritionJpaEntity, Long> {

    Optional<ProductNutritionJpaEntity> findByProductId(Long productId);
}
