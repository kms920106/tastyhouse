package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryPersistencePort;

@Repository
class ProductOptionGroupMergeHistoryPersistenceAdapter implements ProductOptionGroupMergeHistoryPersistencePort {

    private final ProductOptionGroupMergeHistoryJpaRepository jpaRepository;

    public ProductOptionGroupMergeHistoryPersistenceAdapter(
        ProductOptionGroupMergeHistoryJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history) {
        ProductOptionGroupMergeHistoryJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeHistoryMapper.toEntity(history));
        return ProductOptionGroupMergeHistoryMapper.toDomain(saved);
    }

    @Override
    public List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId) {
        return jpaRepository.findAllByShopIdOrderByCreatedAtDesc(shopId.value()).stream()
            .map(ProductOptionGroupMergeHistoryMapper::toDomain)
            .toList();
    }
}
