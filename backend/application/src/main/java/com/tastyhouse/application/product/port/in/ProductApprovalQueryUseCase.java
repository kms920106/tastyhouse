package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ProductApprovalQueryUseCase {

    PageResult<ProductImageChangeRequestResult> getImageChangeRequests(
        String status,
        int page,
        int size
    );

    PageResult<ProductVegetarianRequestResult> getVegetarianRequests(
        String status,
        int page,
        int size
    );

    PageResult<ProductRepresentativeRequestResult> getRepresentativeRequests(
        String status,
        int page,
        int size
    );
}
