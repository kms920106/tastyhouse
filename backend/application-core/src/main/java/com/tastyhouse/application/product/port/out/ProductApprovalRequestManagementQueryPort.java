package com.tastyhouse.application.product.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductApprovalRequestManagementQueryPort {

    PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(String status, PageQuery pageQuery);

    PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(String status, PageQuery pageQuery);

    PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(String status, PageQuery pageQuery);
}
