package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductCategoryCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductCategoryOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductCategoryCommandService implements ProductCategoryCommandUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryCommandService(
        ProductRegistrationService productRegistrationService,
        ProductCategoryRepository productCategoryRepository,
        ProductRepository productRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createProductCategory(ProductCategoryOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String name = command.name();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);

        ProductCategory created = productRegistrationService.createProductCategory(
            ShopId.of(shopId),
            name,
            description,
            nextSort(shopId),
            DEFAULT_VISIBLE
        );
        return created.getId();
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
        productCategoryRepository.save(category);
    }

    @Override
    public void deleteProductCategory(ProductCategoryDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long productCategoryId = command.productCategoryId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductCategory category = loadOwnedCategory(shopId, productCategoryId);
        if (productRepository.countByCategoryId(ProductCategoryId.of(productCategoryId)) > 0) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_HAS_PRODUCTS);
        }
        productCategoryRepository.delete(category);
    }

    private ProductCategory loadOwnedCategory(Long shopId, Long productCategoryId) {
        ProductCategory category = productCategoryRepository.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        if (!category.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
        return category;
    }

    private Integer nextSort(Long shopId) {
        return productCategoryRepository.findAllByShopId(ShopId.of(shopId)).size();
    }
}
