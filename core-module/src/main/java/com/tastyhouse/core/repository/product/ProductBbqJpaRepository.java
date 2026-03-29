package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductBbq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBbqJpaRepository extends JpaRepository<ProductBbq, Long> {
}
