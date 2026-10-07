package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductImageChangeRequestListQueryUseCase {

    PageResult<ProductImageChangeRequestResult> getImageChangeRequests(
        String status,
        int page,
        int size
    );
}
