package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProductNutritionJpaRepository extends JpaRepository<ProductNutritionJpaEntity, Long> {
}
