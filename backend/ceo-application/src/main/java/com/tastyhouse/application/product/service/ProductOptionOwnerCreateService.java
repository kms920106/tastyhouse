package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.in.ProductOptionOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOwnerCreateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionOwnerCreateService implements ProductOptionOwnerCreateUseCase {

    private static final boolean DEFAULT_SOLD_OUT = false;
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionOwnerCreateService(
        ProductRegistrationService productRegistrationService,
        ProductOptionPersistencePort productOptionPersistencePort,
        CupDepositPolicy cupDepositPolicy,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public Long createProductOption(ProductOptionOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long optionGroupId = command.optionGroupId();
        String name = command.name();
        Integer additionalPrice = command.additionalPrice();
        Integer cupCount = command.cupCount();
        Integer personalCupDiscountAmount = command.personalCupDiscountAmount();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
        CupDepositOptionRule.validateOptionValues(
            group, additionalPrice, cupCount, personalCupDiscountAmount, cupDepositPolicy
        );

        return productRegistrationService.saveProductOption(
            ProductOptionGroupId.of(optionGroupId),
            name,
            additionalPrice,
            nextSort(optionGroupId),
            DEFAULT_SOLD_OUT,
            DEFAULT_VISIBLE,
            cupCount,
            personalCupDiscountAmount
        );
    }

    private Integer nextSort(Long optionGroupId) {
        return productOptionPersistencePort.findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).size();
    }
}
