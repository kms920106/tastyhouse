package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductNutritionOwnerDetailQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductNutritionViewResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductNutritionOwnerDetailQueryService implements ProductNutritionOwnerDetailQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionOwnerDetailQueryService(ProductOwnerQueryPort productOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductNutritionViewResult getNutrition(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        return productOwnerQueryPort.findNutrition(productId)
            .map(dto -> new ProductNutritionViewResult(dto, productOwnerQueryPort.findAllergenTypes(productId)))
            .orElse(null);
    }

    private void validateProductOwnedByShop(Long shopId, Long productId) {
        boolean owned = productOwnerQueryPort.existsProductInShop(productId, shopId);
        if (!owned) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
