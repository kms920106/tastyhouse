package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductOptionGroupMergeHistoryRepositoryImpl implements ProductOptionGroupMergeHistoryRepository {
    private final ProductOptionGroupMergeHistoryJpaRepository jpaRepository;

    public ProductOptionGroupMergeHistoryRepositoryImpl(
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
