package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryState;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryStatePort;

@Repository
public class ProductOptionGroupMergeHistoryStatePortImpl implements ProductOptionGroupMergeHistoryStatePort {
    private final ProductOptionGroupMergeHistoryJpaRepository jpaRepository;

    public ProductOptionGroupMergeHistoryStatePortImpl(
        ProductOptionGroupMergeHistoryJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeHistoryState save(ProductOptionGroupMergeHistoryState state) {
        ProductOptionGroupMergeHistoryJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeHistoryMapper.toEntity(state));
        return ProductOptionGroupMergeHistoryMapper.toState(saved);
    }

    @Override
    public List<ProductOptionGroupMergeHistoryState> findAllByShopId(Long shopId) {
        return jpaRepository.findAllByShopIdOrderByCreatedAtDesc(shopId).stream()
            .map(ProductOptionGroupMergeHistoryMapper::toState)
            .toList();
    }
}
