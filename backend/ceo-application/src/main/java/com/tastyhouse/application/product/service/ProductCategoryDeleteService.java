package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductCategoryDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryDeleteUseCase;
import com.tastyhouse.application.product.port.out.write.ProductCategoryLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductCategorySavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductCategoryDeleteService implements ProductCategoryDeleteUseCase {

    private final ProductCategoryLoadPort productCategoryLoadPort;
    private final ProductCategorySavePort productCategorySavePort;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryDeleteService(
        ProductCategoryLoadPort productCategoryLoadPort,
        ProductCategorySavePort productCategorySavePort,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productCategoryLoadPort = productCategoryLoadPort;
        this.productCategorySavePort = productCategorySavePort;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void deleteProductCategory(ProductCategoryDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long productCategoryId = command.productCategoryId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductCategory category = loadOwnedCategory(shopId, productCategoryId);
        if (productLoadPort.countByCategoryId(ProductCategoryId.of(productCategoryId)) > 0) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_CATEGORY_HAS_PRODUCTS);
        }
        productCategorySavePort.delete(category);
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
