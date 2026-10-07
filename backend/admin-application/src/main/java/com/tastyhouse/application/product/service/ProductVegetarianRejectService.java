package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.application.product.port.in.ProductVegetarianRejectCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianRejectUseCase;

@Service
@Transactional
class ProductVegetarianRejectService implements ProductVegetarianRejectUseCase {

    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductVegetarianRejectService(ProductVegetarianApprovalService productVegetarianApprovalService) {
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    @Override
    public void rejectVegetarian(ProductVegetarianRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.reject(requestId, rejectReason);
    }
}
