package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductNutritionDeleteService implements ProductNutritionDeleteUseCase {

    private final ProductNutritionService productNutritionService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionDeleteService(
        ProductNutritionService productNutritionService,
        ProductPersistencePort productPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productNutritionService = productNutritionService;
        this.productPersistencePort = productPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void deleteNutrition(ProductNutritionDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        productNutritionService.deleteNutrition(ProductId.of(productId));
    }

    private void validateProductOwnedByShop(Long shopId, Long productId) {
        Product product = productPersistencePort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
