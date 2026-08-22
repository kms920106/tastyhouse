package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeExclusionRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 추천 제외 write 어댑터.
 *
 * <p>append-only이므로 {@code save}는 insert 전용이다. 같은 서명을 다시 제외하려는 요청은
 * 서비스가 {@code findByShopIdAndGroupSignature}로 먼저 걸러 멱등하게 처리한다
 * ({@code UNIQUE (shop_id, group_signature)}가 최종 방어선이다).
 */
@Repository
public class ProductOptionGroupMergeExclusionRepositoryImpl
    implements ProductOptionGroupMergeExclusionRepository {

    private final ProductOptionGroupMergeExclusionJpaRepository jpaRepository;

    public ProductOptionGroupMergeExclusionRepositoryImpl(
        ProductOptionGroupMergeExclusionJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion) {
        ProductOptionGroupMergeExclusionJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeExclusionMapper.toEntity(exclusion));
        return ProductOptionGroupMergeExclusionMapper.toDomain(saved);
    }

    @Override
    public Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    ) {
        return jpaRepository.findByShopIdAndGroupSignature(shopId.value(), groupSignature)
            .map(ProductOptionGroupMergeExclusionMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroupMergeExclusion> findAllByShopId(ShopId shopId) {
        return jpaRepository.findAllByShopId(shopId.value()).stream()
            .map(ProductOptionGroupMergeExclusionMapper::toDomain)
            .toList();
    }
}
