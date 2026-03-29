package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
}
