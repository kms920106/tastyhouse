package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.application.product.port.in.ProductImageChangeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductImageChangeApproveUseCase;

@Service
@Transactional
class ProductImageChangeApproveService implements ProductImageChangeApproveUseCase {

    private final ProductImageApprovalService productImageApprovalService;

    public ProductImageChangeApproveService(ProductImageApprovalService productImageApprovalService) {
        this.productImageApprovalService = productImageApprovalService;
    }

    @Override
    public void approveImageChange(ProductImageChangeApproveCommand command) {
        Long id = command.requestId();
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.approve(requestId);
    }
}
