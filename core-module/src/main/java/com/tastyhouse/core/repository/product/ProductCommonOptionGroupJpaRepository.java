package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCommonOptionGroupJpaRepository extends JpaRepository<ProductCommonOptionGroup, Long> {
}
