package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductImageCreateCommand;
import com.tastyhouse.application.product.port.in.ProductImageCreateUseCase;

@Service
@Transactional
class ProductImageCreateService implements ProductImageCreateUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductImageCreateService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public Long createProductImage(ProductImageCreateCommand command) {
        Long id = command.productId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        boolean visible = command.visible();

        return productRegistrationService.saveProductImage(
            ProductId.of(id), UploadedFileId.of(imageFileId), sort, visible
        );
    }
}
