package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductVegetarianRequestListQueryService implements ProductVegetarianRequestListQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductVegetarianRequestListQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PageResult<ProductVegetarianRequestResult> getVegetarianRequests(
        String status,
        int page,
        int size
    ) {
        String approvalStatus = demoteStatus(status);

        return productManagementQueryPort.findVegetarianRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    private String demoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status).name();
    }
}
