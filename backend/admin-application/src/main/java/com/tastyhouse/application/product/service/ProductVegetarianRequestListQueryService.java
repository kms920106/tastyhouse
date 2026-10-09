package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductApprovalRequestManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductVegetarianRequestListQueryService implements ProductVegetarianRequestListQueryUseCase {

    private final ProductApprovalRequestManagementQueryPort productApprovalRequestManagementQueryPort;

    public ProductVegetarianRequestListQueryService(
        ProductApprovalRequestManagementQueryPort productApprovalRequestManagementQueryPort
    ) {
        this.productApprovalRequestManagementQueryPort = productApprovalRequestManagementQueryPort;
    }

    @Override
    public PageResult<ProductVegetarianRequestResult> getVegetarianRequests(
        String status,
        int page,
        int size
    ) {
        String approvalStatus = demoteStatus(status);

        return productApprovalRequestManagementQueryPort.findVegetarianRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    private String demoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status).name();
    }
}
