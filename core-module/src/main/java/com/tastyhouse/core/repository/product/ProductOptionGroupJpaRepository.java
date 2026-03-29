package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionGroupJpaRepository extends JpaRepository<ProductOptionGroup, Long> {
}
