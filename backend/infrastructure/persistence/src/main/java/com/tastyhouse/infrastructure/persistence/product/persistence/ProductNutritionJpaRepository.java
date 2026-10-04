package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProductNutritionJpaRepository extends JpaRepository<ProductNutritionJpaEntity, Long> {

    Optional<ProductNutritionJpaEntity> findByProductId(Long productId);
}
