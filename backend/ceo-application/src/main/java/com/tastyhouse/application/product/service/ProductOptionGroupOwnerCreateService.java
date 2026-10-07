package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerCreateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupOwnerCreateService implements ProductOptionGroupOwnerCreateUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private static final Integer NEXT_SORT_APPENDS_TO_TAIL = null;

    private final ProductRegistrationService productRegistrationService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopPersistencePort shopPersistencePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupOwnerCreateService(
        ProductRegistrationService productRegistrationService,
        ProductPersistencePort productPersistencePort,
        ShopPersistencePort shopPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productPersistencePort = productPersistencePort;
        this.shopPersistencePort = shopPersistencePort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createProductOptionGroup(ProductOptionGroupOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        String name = command.name();
        String description = command.description();
        boolean required = Boolean.TRUE.equals(command.required());
        boolean multipleSelect = Boolean.TRUE.equals(command.multipleSelect());
        Integer minSelect = command.minSelect();
        Integer maxSelect = command.maxSelect();
        String groupType = command.groupType();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);
        validateSelectRange(minSelect, maxSelect);

        ProductOptionGroupType resolvedGroupType = ProductOptionGroupType.from(groupType);

        if (resolvedGroupType.isCupDeposit()) {
            loadShop(shopId).validateCupDepositEnabled();
        }
        CupDepositOptionRule.validateDepositGroupSelectRange(
            resolvedGroupType, required, multipleSelect, minSelect, maxSelect
        );

        Product product = loadOwnedProduct(shopId, productId);
        ProductOptionGroup created = productRegistrationService.saveProductOptionGroup(
            ProductId.of(product.getId()),
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            NEXT_SORT_APPENDS_TO_TAIL,
            DEFAULT_VISIBLE,
            resolvedGroupType
        );
        return created.getId();
    }

    private Shop loadShop(Long shopId) {
        return shopPersistencePort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }

    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productPersistencePort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private void validateSelectRange(Integer minSelect, Integer maxSelect) {
        if (minSelect != null && maxSelect != null && minSelect > maxSelect) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_SELECT_RANGE_INVALID);
        }
    }
}
