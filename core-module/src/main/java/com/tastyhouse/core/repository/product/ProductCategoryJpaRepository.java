package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, Long> {
}
