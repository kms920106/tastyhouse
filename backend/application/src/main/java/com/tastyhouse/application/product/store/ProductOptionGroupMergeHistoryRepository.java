package com.tastyhouse.application.product.store;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductOptionGroupMergeHistoryRepository {
    ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history);

    List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId);
}
