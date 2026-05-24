package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductBbqJpaRepository extends JpaRepository<ProductBbq, Long> {
}
