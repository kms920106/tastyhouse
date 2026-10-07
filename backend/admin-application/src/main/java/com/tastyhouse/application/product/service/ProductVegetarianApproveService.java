package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.application.product.port.in.ProductVegetarianApproveCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianApproveUseCase;

@Service
@Transactional
class ProductVegetarianApproveService implements ProductVegetarianApproveUseCase {

    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductVegetarianApproveService(ProductVegetarianApprovalService productVegetarianApprovalService) {
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    @Override
    public void approveVegetarian(ProductVegetarianApproveCommand command) {
        Long id = command.requestId();
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.approve(requestId);
    }
}
