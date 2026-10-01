package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionGroupJpaRepository extends JpaRepository<ProductOptionGroupJpaEntity, Long> {

    List<ProductOptionGroupJpaEntity> findAllByIdIn(List<Long> ids);
}
