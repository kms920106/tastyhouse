package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionSelectionRule;
import com.tastyhouse.application.product.port.in.ProductOptionUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionUpdateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionUpdateService implements ProductOptionUpdateUseCase {

    private final ProductOptionLoadPort productOptionLoadPort;
    private final ProductOptionSavePort productOptionSavePort;
    private final ProductOptionGroupLoadPort productOptionGroupLoadPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionUpdateService(
        ProductOptionLoadPort productOptionLoadPort,
        ProductOptionSavePort productOptionSavePort,
        ProductOptionGroupLoadPort productOptionGroupLoadPort,
        CupDepositPolicy cupDepositPolicy,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionLoadPort = productOptionLoadPort;
        this.productOptionSavePort = productOptionSavePort;
        this.productOptionGroupLoadPort = productOptionGroupLoadPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void updateProductOption(ProductOptionUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long optionId = command.optionId();
        Long shopId = command.shopId();
        String name = command.name();
        Integer additionalPrice = command.additionalPrice();
        Integer cupCount = command.cupCount();
        Integer personalCupDiscountAmount = command.personalCupDiscountAmount();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);

        ProductOption option = productOptionGroupOwnershipValidator.loadOwnedOption(shopId, optionId);
        ProductOptionGroup optionGroup = productOptionGroupOwnershipValidator
            .loadOwnedOptionGroup(shopId, option.getOptionGroupId().value());
        CupDepositOptionRule.validateOptionValues(
            optionGroup, additionalPrice, cupCount, personalCupDiscountAmount, cupDepositPolicy
        );

        option.update(
            name,
            additionalPrice,
            option.getSort(),
            option.isSoldOut(),
            option.isVisible(),
            cupCount,
            personalCupDiscountAmount
        );

        validateZeroPriceOptionAfterChange(option);

        productOptionSavePort.save(option);
    }

    private void validateZeroPriceOptionAfterChange(ProductOption changed) {
        ProductOptionGroup group = productOptionGroupLoadPort
            .findById(changed.getOptionGroupId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        if (!group.isRequired()) {
            return;
        }

        List<ProductOption> options = productOptionLoadPort
            .findAllByOptionGroupId(changed.getOptionGroupId()).stream()
            .map(option -> option.getId().equals(changed.getId()) ? changed : option)
            .toList();
        ProductOptionSelectionRule.validateZeroPriceOption(group, options);
    }
}
