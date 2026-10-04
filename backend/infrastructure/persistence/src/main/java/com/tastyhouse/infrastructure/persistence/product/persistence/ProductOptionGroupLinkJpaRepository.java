package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductOptionGroupLinkJpaRepository extends JpaRepository<ProductOptionGroupLinkJpaEntity, Long> {

    Optional<ProductOptionGroupLinkJpaEntity> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    List<ProductOptionGroupLinkJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);

    List<ProductOptionGroupLinkJpaEntity> findAllByOptionGroupId(Long optionGroupId);

    List<ProductOptionGroupLinkJpaEntity> findAllByOptionGroupIdIn(List<Long> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId);
}
