package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommonOptionGroupJpaRepository extends JpaRepository<ProductCommonOptionGroupJpaEntity, Long> {

    List<ProductCommonOptionGroupJpaEntity> findAllByIdIn(List<Long> ids);
}
