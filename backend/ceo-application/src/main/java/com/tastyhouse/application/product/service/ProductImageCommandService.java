package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileUploadOwnerCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageChangeRequestCommand;
import com.tastyhouse.application.product.port.in.ProductImageCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductImageReorderCommand;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductImageCommandService implements ProductImageCommandUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductPersistencePort productPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductImageSpecValidator productImageSpecValidator;
    private final FileUploadOwnerCommandUseCase fileUploadCommandUseCase;

    public ProductImageCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductPersistencePort productPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductImageSpecValidator productImageSpecValidator,
        FileUploadOwnerCommandUseCase fileUploadCommandUseCase
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productPersistencePort = productPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productImageSpecValidator = productImageSpecValidator;
        this.fileUploadCommandUseCase = fileUploadCommandUseCase;
    }

    @Override
    public Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageSpecValidator.validate(file);

        Long imageFileId = fileUploadCommandUseCase.upload(file);
        return productImageApprovalService.requestImageChange(
            ProductId.of(productId), UploadedFileId.of(imageFileId)
        );
    }

    @Override
    public void reorderImages(ProductImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> imageIds = command.imageIds();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageApprovalService.reorderImages(ProductId.of(productId), imageIds);
    }

    @Override
    public void deleteImage(ProductImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductImage image = productImagePersistencePort.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (notOwnedBy(shopId, image.getProductId())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        productImagePersistencePort.delete(image);
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        if (notOwnedBy(shopId, ProductId.of(productId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productPersistencePort.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
