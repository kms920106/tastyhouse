package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProductAllergenJpaRepository extends JpaRepository<ProductAllergenJpaEntity, Long> {
}
