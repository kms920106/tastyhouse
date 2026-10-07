package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductImageDeleteUseCase;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductImageDeleteService implements ProductImageDeleteUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductImageDeleteService(
        ProductPersistencePort productPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void deleteImage(ProductImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductImage image = productImagePersistencePort.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (notOwnedBy(shopId, image.getProductId())) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        productImagePersistencePort.delete(image);
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productPersistencePort.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
