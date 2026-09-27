package com.tastyhouse.application.product.port.out.write;

import java.util.List;

public interface ProductOptionGroupMergeHistoryStatePort {
    ProductOptionGroupMergeHistoryState save(ProductOptionGroupMergeHistoryState history);

    List<ProductOptionGroupMergeHistoryState> findAllByShopId(Long shopId);
}
