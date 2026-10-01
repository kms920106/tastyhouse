package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommonOptionGroupLinkJpaRepository extends JpaRepository<ProductCommonOptionGroupLinkJpaEntity, Long> {

    Optional<ProductCommonOptionGroupLinkJpaEntity> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    List<ProductCommonOptionGroupLinkJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);

    List<ProductCommonOptionGroupLinkJpaEntity> findAllByOptionGroupId(Long optionGroupId);

    List<ProductCommonOptionGroupLinkJpaEntity> findAllByOptionGroupIdIn(List<Long> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId);
}
