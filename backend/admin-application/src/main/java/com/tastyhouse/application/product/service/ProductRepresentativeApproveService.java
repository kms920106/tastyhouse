package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.application.product.port.in.ProductRepresentativeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeApproveUseCase;

@Service
@Transactional
class ProductRepresentativeApproveService implements ProductRepresentativeApproveUseCase {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;

    public ProductRepresentativeApproveService(ProductRepresentativeApprovalService productRepresentativeApprovalService) {
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
    }

    @Override
    public void approveRepresentative(ProductRepresentativeApproveCommand command) {
        Long id = command.requestId();
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.approve(requestId);
    }
}
