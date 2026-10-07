package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionSelectionRule;
import com.tastyhouse.application.product.port.in.ProductOptionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionDeleteUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionDeleteService implements ProductOptionDeleteUseCase {

    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionDeleteService(
        ProductOptionPersistencePort productOptionPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void deleteProductOption(ProductOptionDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long optionId = command.optionId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductOption option = productOptionGroupOwnershipValidator.loadOwnedOption(shopId, optionId);
        Long optionGroupId = option.getOptionGroupId().value();
        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);

        List<ProductOption> groupOptions =
            productOptionPersistencePort.findAllByOptionGroupId(group.getProductOptionGroupId());

        ProductOptionSelectionRule.validateRemainingAfterBlocking(group, option, groupOptions);

        option.hide();

        List<ProductOption> groupOptionsAfterHide = groupOptions.stream()
            .map(candidate -> candidate.getId().equals(option.getId()) ? option : candidate)
            .toList();
        ProductOptionSelectionRule.validateZeroPriceOption(group, groupOptionsAfterHide);

        productOptionPersistencePort.save(option);
    }
}
