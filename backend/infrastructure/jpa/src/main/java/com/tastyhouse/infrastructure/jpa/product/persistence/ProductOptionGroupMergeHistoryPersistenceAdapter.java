package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistorySavePort;

@Repository
class ProductOptionGroupMergeHistoryPersistenceAdapter implements ProductOptionGroupMergeHistorySavePort {

    private final ProductOptionGroupMergeHistoryJpaRepository jpaRepository;

    public ProductOptionGroupMergeHistoryPersistenceAdapter(ProductOptionGroupMergeHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history) {
        ProductOptionGroupMergeHistoryJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeHistoryMapper.toEntity(history));
        return ProductOptionGroupMergeHistoryMapper.toDomain(saved);
    }
}
