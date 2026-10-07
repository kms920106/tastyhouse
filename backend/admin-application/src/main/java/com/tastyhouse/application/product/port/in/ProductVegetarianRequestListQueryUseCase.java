package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductVegetarianRequestListQueryUseCase {

    PageResult<ProductVegetarianRequestResult> getVegetarianRequests(
        String status,
        int page,
        int size
    );
}
