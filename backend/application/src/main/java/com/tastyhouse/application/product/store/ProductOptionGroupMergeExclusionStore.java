package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionStatePort;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductOptionGroupMergeExclusionStore implements ProductOptionGroupMergeExclusionRepository {
    private final ProductOptionGroupMergeExclusionStatePort productOptionGroupMergeExclusionStatePort;

    public ProductOptionGroupMergeExclusionStore(
        ProductOptionGroupMergeExclusionStatePort productOptionGroupMergeExclusionStatePort
    ) {
        this.productOptionGroupMergeExclusionStatePort = productOptionGroupMergeExclusionStatePort;
    }

    @Override
    public ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion) {
        return ProductOptionGroupMergeExclusionStateMapper.toDomain(
            productOptionGroupMergeExclusionStatePort.save(ProductOptionGroupMergeExclusionStateMapper.toState(exclusion)));
    }

    @Override
    public Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    ) {
        return productOptionGroupMergeExclusionStatePort.findByShopIdAndGroupSignature(shopId.value(), groupSignature)
            .map(ProductOptionGroupMergeExclusionStateMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroupMergeExclusion> findAllByShopId(ShopId shopId) {
        return productOptionGroupMergeExclusionStatePort.findAllByShopId(shopId.value()).stream()
            .map(ProductOptionGroupMergeExclusionStateMapper::toDomain)
            .toList();
    }
}
