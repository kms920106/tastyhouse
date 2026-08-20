package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {

    /** 순서 변경(replace-all)과 "맨 뒤 sort" 산출이 집합 전체를 보므로 정렬을 보장한다. */
    List<ProductImageJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);
}
