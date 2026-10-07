package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.product.port.in.ProductImageChangeRequestCommand;
import com.tastyhouse.application.product.port.in.ProductImageChangeRequestUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductImageChangeRequestService implements ProductImageChangeRequestUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductImageSpecValidator productImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;

    public ProductImageChangeRequestService(
        ProductImageApprovalService productImageApprovalService,
        ProductPersistencePort productPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductImageSpecValidator productImageSpecValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productPersistencePort = productPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productImageSpecValidator = productImageSpecValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
    }

    @Override
    public Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageSpecValidator.validate(file);

        Long imageFileId = fileOwnerUploadUseCase.upload(file);
        return productImageApprovalService.requestImageChange(
            ProductId.of(productId), UploadedFileId.of(imageFileId)
        );
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        if (notOwnedBy(shopId, ProductId.of(productId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productPersistencePort.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
