package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopFoodTypeCategoryReader;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductVegetarianRequestService implements ProductVegetarianRequestUseCase {

    private final ProductVegetarianApprovalService productVegetarianApprovalService;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;

    public ProductVegetarianRequestService(
        ProductVegetarianApprovalService productVegetarianApprovalService,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader
    ) {
        this.productVegetarianApprovalService = productVegetarianApprovalService;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopFoodTypeCategoryReader = shopFoodTypeCategoryReader;
    }

    @Override
    public Long requestVegetarian(ProductVegetarianRequestCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        String vegetarianType = command.vegetarianType();
        String ingredients = command.ingredients();
        String description = command.description();

        requireOwnedProduct(ceoId, shopId, productId);
        Set<String> shopCategoryNames = shopFoodTypeCategoryReader.readCategoryNames(shopId);

        return productVegetarianApprovalService.requestVegetarian(
            ProductId.of(productId),
            VegetarianType.from(vegetarianType),
            ingredients,
            description,
            shopCategoryNames
        );
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productLoadPort.findAllActiveByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
