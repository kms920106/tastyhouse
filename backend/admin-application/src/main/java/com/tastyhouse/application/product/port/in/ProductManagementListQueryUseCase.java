package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductManagementListQueryUseCase {

    PageResult<ProductListItemResult> getProducts(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut,
        int page,
        int size
    );
}
