package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 이력 write 어댑터.
 *
 * <p>append-only이므로 {@code save}는 <b>insert 전용</b>이다 — load-copy-save의 update 분기를 두지
 * 않는다. 이력 행이 사후에 바뀌면 감사 근거로서의 가치가 사라진다.
 */
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
    public List<ProductOptionGroupMergeHistory> findAllByMergedOptionGroupId(
        ProductOptionGroupId mergedOptionGroupId
    ) {
        return jpaRepository.findAllByMergedOptionGroupId(mergedOptionGroupId.value()).stream()
            .map(ProductOptionGroupMergeHistoryMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId) {
        return jpaRepository.findAllByShopIdOrderByCreatedAtDesc(shopId.value()).stream()
            .map(ProductOptionGroupMergeHistoryMapper::toDomain)
            .toList();
    }
}
