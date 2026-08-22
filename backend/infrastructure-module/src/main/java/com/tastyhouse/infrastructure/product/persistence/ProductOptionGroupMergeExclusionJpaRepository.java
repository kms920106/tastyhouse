package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionGroupMergeExclusionJpaRepository
    extends JpaRepository<ProductOptionGroupMergeExclusionJpaEntity, Long> {

    Optional<ProductOptionGroupMergeExclusionJpaEntity> findByShopIdAndGroupSignature(
        Long shopId,
        String groupSignature
    );

    List<ProductOptionGroupMergeExclusionJpaEntity> findAllByShopId(Long shopId);
}
