package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductOptionGroupMergeHistoryPersistencePort {

    ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history);

    List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId);
}
