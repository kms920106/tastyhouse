package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionState;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionStatePort;

@Repository
public class ProductOptionGroupMergeExclusionStatePortImpl
    implements ProductOptionGroupMergeExclusionStatePort {
    private final ProductOptionGroupMergeExclusionJpaRepository jpaRepository;

    public ProductOptionGroupMergeExclusionStatePortImpl(
        ProductOptionGroupMergeExclusionJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeExclusionState save(ProductOptionGroupMergeExclusionState state) {
        ProductOptionGroupMergeExclusionJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeExclusionMapper.toEntity(state));
        return ProductOptionGroupMergeExclusionMapper.toState(saved);
    }

    @Override
    public Optional<ProductOptionGroupMergeExclusionState> findByShopIdAndGroupSignature(
        Long shopId,
        String groupSignature
    ) {
        return jpaRepository.findByShopIdAndGroupSignature(shopId, groupSignature)
            .map(ProductOptionGroupMergeExclusionMapper::toState);
    }

    @Override
    public List<ProductOptionGroupMergeExclusionState> findAllByShopId(Long shopId) {
        return jpaRepository.findAllByShopId(shopId).stream()
            .map(ProductOptionGroupMergeExclusionMapper::toState)
            .toList();
    }
}
