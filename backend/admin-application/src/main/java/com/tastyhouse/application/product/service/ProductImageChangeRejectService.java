package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.application.product.port.in.ProductImageChangeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductImageChangeRejectUseCase;

@Service
@Transactional
class ProductImageChangeRejectService implements ProductImageChangeRejectUseCase {

    private final ProductImageApprovalService productImageApprovalService;

    public ProductImageChangeRejectService(ProductImageApprovalService productImageApprovalService) {
        this.productImageApprovalService = productImageApprovalService;
    }

    @Override
    public void rejectImageChange(ProductImageChangeRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.reject(requestId, rejectReason);
    }
}
