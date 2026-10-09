package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.in.ProductOptionCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionManagementCreateCommand;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ProductOptionCreateService implements ProductOptionCreateUseCase {

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionGroupLoadPort productOptionGroupLoadPort;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductOptionCreateService(
        ProductRegistrationService productRegistrationService,
        ProductOptionGroupLoadPort productOptionGroupLoadPort,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionGroupLoadPort = productOptionGroupLoadPort;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    @Override
    public Long createProductOption(ProductOptionManagementCreateCommand command) {
        Long groupId = command.optionGroupId();
        String name = command.name();
        Integer additionalPrice = command.additionalPrice();
        Integer sort = command.sort();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer cupCount = command.cupCount();
        Integer personalCupDiscountAmount = command.personalCupDiscountAmount();

        ProductOptionGroup optionGroup = productOptionGroupLoadPort
            .findById(ProductOptionGroupId.of(groupId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        CupDepositOptionRule.validateOptionValues(
            optionGroup, additionalPrice, cupCount, personalCupDiscountAmount, cupDepositPolicy
        );

        return productRegistrationService.saveProductOption(
            ProductOptionGroupId.of(groupId),
            name,
            additionalPrice,
            sort,
            soldOut,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }
}
