package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.product.port.in.ProductApprovalQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ProductApprovalQueryService implements ProductApprovalQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductApprovalQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PageResult<ProductImageChangeRequestResult> getImageChangeRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        return productManagementQueryPort.findImageChangeRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    @Override
    public PageResult<ProductVegetarianRequestResult> getVegetarianRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        return productManagementQueryPort.findVegetarianRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    @Override
    public PageResult<ProductRepresentativeRequestResult> getRepresentativeRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        return productManagementQueryPort.findRepresentativeRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    private ApprovalStatus promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status);
    }
}
