package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductCategoryUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryUpdateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductCategoryLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductCategorySavePort;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductCategoryUpdateService implements ProductCategoryUpdateUseCase {

    private final ProductCategoryLoadPort productCategoryLoadPort;
    private final ProductCategorySavePort productCategorySavePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryUpdateService(
        ProductCategoryLoadPort productCategoryLoadPort,
        ProductCategorySavePort productCategorySavePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productCategoryLoadPort = productCategoryLoadPort;
        this.productCategorySavePort = productCategorySavePort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateProductCategory(ProductCategoryUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long productCategoryId = command.productCategoryId();
        Long shopId = command.shopId();
        String name = command.name();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);

        ProductCategory category = loadOwnedCategory(shopId, productCategoryId);
        category.changeDetails(name, description);
        productCategorySavePort.save(category);
    }

    private ProductCategory loadOwnedCategory(Long shopId, Long productCategoryId) {
        ProductCategory category = productCategoryLoadPort.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        if (!category.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(CeoErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
        return category;
    }
}
