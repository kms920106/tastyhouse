package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOwnerUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductOwnerUpdateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOwnerUpdateService implements ProductOwnerUpdateUseCase {

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ProductNameValidator productNameValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOwnerUpdateService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ProductNameValidator productNameValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.productNameValidator = productNameValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateProduct(ProductOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long productId = command.productId();
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String composition = command.composition();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        Boolean singleServing = command.singleServing();
        Integer spiciness = command.spiciness();
        Boolean representative = command.representative();
        Boolean ratingExcluded = command.ratingExcluded();
        String weightText = command.weightText();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateTexts(name, composition, description);
        productNameValidator.validateForUpdate(shopId, productId, name);

        Product product = loadOwnedProduct(shopId, productId);
        ProductCategoryId categoryId = toProductCategoryId(productCategoryId);
        boolean categoryChanged = !isSameCategory(product.getProductCategoryId(), categoryId);

        product.changeDetails(
            categoryId,
            name,
            composition,
            description,
            originalPrice,
            discountPrice,
            null,
            Boolean.TRUE.equals(singleServing),
            spiciness,
            Boolean.TRUE.equals(representative),
            Boolean.TRUE.equals(ratingExcluded),
            weightText
        );
        if (categoryChanged) {
            product.relocate(categoryId, nextSort(shopId, categoryId));
        }
        productSavePort.save(product);
    }

    private void validateTexts(String name, String composition, String description) {
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(composition);
        prohibitedWordValidator.validate(description);
    }

    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productLoadPort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private Integer nextSort(Long shopId, ProductCategoryId productCategoryId) {
        return productLoadPort.findAllByShopIdAndCategoryId(ShopId.of(shopId), productCategoryId).size();
    }

    private boolean isSameCategory(ProductCategoryId current, ProductCategoryId requested) {
        if (current == null || requested == null) {
            return current == requested;
        }
        return current.equals(requested);
    }

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }
}
