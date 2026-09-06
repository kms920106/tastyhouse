package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductVegetarianClearCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestCommand;
import com.tastyhouse.application.shop.service.ShopFoodTypeCategoryReader;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductVegetarianCommandService implements ProductVegetarianCommandUseCase {

    private final ProductVegetarianApprovalService productVegetarianApprovalService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;

    public ProductVegetarianCommandService(
        ProductVegetarianApprovalService productVegetarianApprovalService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader
    ) {
        this.productVegetarianApprovalService = productVegetarianApprovalService;
        this.productRepository = productRepository;
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

    @Override
    public void clearVegetarian(ProductVegetarianClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productVegetarianApprovalService.clearVegetarian(ProductId.of(productId));
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productRepository.findAllByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
