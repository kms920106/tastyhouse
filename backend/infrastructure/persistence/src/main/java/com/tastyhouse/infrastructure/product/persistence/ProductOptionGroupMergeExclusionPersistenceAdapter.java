package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionPersistencePort;

@Repository
public class ProductOptionGroupMergeExclusionPersistenceAdapter
    implements ProductOptionGroupMergeExclusionPersistencePort {
    private final ProductOptionGroupMergeExclusionJpaRepository jpaRepository;

    public ProductOptionGroupMergeExclusionPersistenceAdapter(
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
