package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.product.port.in.ProductImageChangeRequestCommand;
import com.tastyhouse.application.product.port.in.ProductImageCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductImageReorderCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductImageCommandService implements ProductImageCommandUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductImageSpecValidator productImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ProductImageCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductImageSpecValidator productImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productImageSpecValidator = productImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    @Override
    public Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageSpecValidator.validate(file);

        Long imageFileId = fileUploadCommandService.upload(file);
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

        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (notOwnedBy(shopId, image.getProductId())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        productImageRepository.delete(image);
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        if (notOwnedBy(shopId, ProductId.of(productId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
