package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionSelectionRule;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUpdateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupUpdateService implements ProductOptionGroupUpdateUseCase {

    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupUpdateService(
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void updateProductOptionGroup(ProductOptionGroupUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long optionGroupId = command.optionGroupId();
        Long shopId = command.shopId();
        String name = command.name();
        String description = command.description();
        boolean required = Boolean.TRUE.equals(command.required());
        boolean multipleSelect = Boolean.TRUE.equals(command.multipleSelect());
        Integer minSelect = command.minSelect();
        Integer maxSelect = command.maxSelect();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);
        validateSelectRange(minSelect, maxSelect);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);

        CupDepositOptionRule.validateDepositGroupSelectRange(
            group.getGroupType(), required, multipleSelect, minSelect, maxSelect
        );

        group.update(
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            group.getSort(),
            group.isVisible()
        );

        List<ProductOption> groupOptions =
            productOptionPersistencePort.findAllByOptionGroupId(group.getProductOptionGroupId());
        ProductOptionSelectionRule.validateZeroPriceOption(group, groupOptions);

        productOptionGroupPersistencePort.save(group);
    }

    private void validateSelectRange(Integer minSelect, Integer maxSelect) {
        if (minSelect != null && maxSelect != null && minSelect > maxSelect) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_SELECT_RANGE_INVALID);
        }
    }
}
