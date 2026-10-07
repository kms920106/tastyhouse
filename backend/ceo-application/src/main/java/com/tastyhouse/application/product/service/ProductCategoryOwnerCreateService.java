package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductCategoryOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryOwnerCreateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductCategoryOwnerCreateService implements ProductCategoryOwnerCreateUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryPersistencePort productCategoryPersistencePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryOwnerCreateService(
        ProductRegistrationService productRegistrationService,
        ProductCategoryPersistencePort productCategoryPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productCategoryPersistencePort = productCategoryPersistencePort;
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

    private Integer nextSort(Long shopId) {
        return productCategoryPersistencePort.findAllByShopId(ShopId.of(shopId)).size();
    }
}
