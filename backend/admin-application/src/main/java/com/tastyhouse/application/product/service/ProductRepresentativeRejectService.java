package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRejectUseCase;

@Service
@Transactional
class ProductRepresentativeRejectService implements ProductRepresentativeRejectUseCase {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;

    public ProductRepresentativeRejectService(ProductRepresentativeApprovalService productRepresentativeApprovalService) {
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
    }

    @Override
    public void rejectRepresentative(ProductRepresentativeRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.reject(requestId, rejectReason);
    }
}
