package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductOptionGroupMergeHistoryJpaRepository
    extends JpaRepository<ProductOptionGroupMergeHistoryJpaEntity, Long> {

    List<ProductOptionGroupMergeHistoryJpaEntity> findAllByShopIdOrderByCreatedAtDesc(Long shopId);
}
