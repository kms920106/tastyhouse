package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductImageStatusResult;
import com.tastyhouse.application.product.port.in.ProductImageQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductImageManagementResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductImageQueryService implements ProductImageQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductImageQueryService(ProductOwnerQueryPort productOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductImageStatusResult getImageStatus(Long ceoId, Long shopId, Long productId) {
        requireOwnedProduct(ceoId, shopId, productId);

        List<ProductImageManagementResult> images = productOwnerQueryPort.findProductImagesForManagement(productId);
        List<ProductImageChangeRequestResult> requests = productOwnerQueryPort.findImageChangeRequests(productId);

        return new ProductImageStatusResult(images, requests);
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        boolean owned = productOwnerQueryPort.existsProductInShop(productId, shopId);
        if (!owned) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
