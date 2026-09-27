package com.tastyhouse.application.product.store;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryStatePort;

public class ProductOptionGroupMergeHistoryStore implements ProductOptionGroupMergeHistoryRepository {
    private final ProductOptionGroupMergeHistoryStatePort productOptionGroupMergeHistoryStatePort;

    public ProductOptionGroupMergeHistoryStore(
        ProductOptionGroupMergeHistoryStatePort productOptionGroupMergeHistoryStatePort
    ) {
        this.productOptionGroupMergeHistoryStatePort = productOptionGroupMergeHistoryStatePort;
    }

    @Override
    public ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history) {
        return ProductOptionGroupMergeHistoryStateMapper.toDomain(
            productOptionGroupMergeHistoryStatePort.save(ProductOptionGroupMergeHistoryStateMapper.toState(history)));
    }

    @Override
    public List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId) {
        return productOptionGroupMergeHistoryStatePort.findAllByShopId(shopId.value()).stream()
            .map(ProductOptionGroupMergeHistoryStateMapper::toDomain)
            .toList();
    }
}
