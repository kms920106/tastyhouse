package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
}
