package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductApprovalCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageChangeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductImageChangeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianApproveCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianRejectCommand;
import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;

@Service
@AdminApp
@Transactional
public class ProductApprovalCommandService implements ProductApprovalCommandUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;
    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;

    public ProductApprovalCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductVegetarianApprovalService productVegetarianApprovalService,
        ProductRepresentativeApprovalService productRepresentativeApprovalService
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
    }

    @Override
    public void approveImageChange(ProductImageChangeApproveCommand command) {
        Long id = command.requestId();
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.approve(requestId);
    }

    @Override
    public void rejectImageChange(ProductImageChangeRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.reject(requestId, rejectReason);
    }

    @Override
    public void approveVegetarian(ProductVegetarianApproveCommand command) {
        Long id = command.requestId();
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.approve(requestId);
    }

    @Override
    public void rejectVegetarian(ProductVegetarianRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.reject(requestId, rejectReason);
    }

    @Override
    public void approveRepresentative(ProductRepresentativeApproveCommand command) {
        Long id = command.requestId();
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.approve(requestId);
    }

    @Override
    public void rejectRepresentative(ProductRepresentativeRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.reject(requestId, rejectReason);
    }
}
