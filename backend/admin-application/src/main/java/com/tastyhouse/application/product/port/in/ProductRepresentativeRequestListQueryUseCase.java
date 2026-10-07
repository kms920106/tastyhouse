package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductRepresentativeRequestListQueryUseCase {

    PageResult<ProductRepresentativeRequestResult> getRepresentativeRequests(
        String status,
        int page,
        int size
    );
}
