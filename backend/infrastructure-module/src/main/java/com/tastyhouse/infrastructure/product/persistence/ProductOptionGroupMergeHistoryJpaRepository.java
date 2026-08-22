package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionGroupMergeHistoryJpaRepository
    extends JpaRepository<ProductOptionGroupMergeHistoryJpaEntity, Long> {

    List<ProductOptionGroupMergeHistoryJpaEntity> findAllByMergedOptionGroupId(Long mergedOptionGroupId);

    List<ProductOptionGroupMergeHistoryJpaEntity> findAllByShopIdOrderByCreatedAtDesc(Long shopId);
}
