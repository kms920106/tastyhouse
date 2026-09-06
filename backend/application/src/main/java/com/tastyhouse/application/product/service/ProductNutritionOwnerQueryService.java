package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;
import com.tastyhouse.application.product.port.out.ProductNutritionViewResult;
import com.tastyhouse.application.product.port.in.ProductNutritionOwnerQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductNutritionOwnerQueryService implements ProductNutritionOwnerQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionOwnerQueryService(ProductOwnerQueryPort productOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
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

    @Override
    public List<ProductAllergenTypeView> getAllergenTypes() {
        return Arrays.stream(AllergenType.values())
            .map(allergenType -> new ProductAllergenTypeView(allergenType.name(), allergenType.getDescription()))
            .toList();
    }

    private void validateProductOwnedByShop(Long shopId, Long productId) {
        boolean owned = productOwnerQueryPort.existsProductInShop(productId, shopId);
        if (!owned) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

}
