package com.tastyhouse.application.product.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionSelectionRule;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.in.ProductOptionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionUpdateCommand;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionCommandService implements ProductOptionCommandUseCase {

    private static final boolean DEFAULT_SOLD_OUT = false;
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionCommandService(
        ProductRegistrationService productRegistrationService,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        CupDepositPolicy cupDepositPolicy,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
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

        productOptionPersistencePort.save(option);
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

    @Override
    public void changeProductOptionOrder(ProductOptionOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long optionGroupId = command.optionGroupId();
        List<Long> optionIds = command.optionIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        Map<Long, ProductOption> byId = productOptionPersistencePort
            .findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        List<Long> requested = distinct(optionIds);
        if (byId.size() != requested.size() || !byId.keySet().containsAll(requested)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOption option = byId.get(requested.get(index));
            option.update(
                option.getName(),
                option.getAdditionalPrice(),
                index,
                option.isSoldOut(),
                option.isVisible(),
                option.getCupCount(),
                option.getPersonalCupDiscountAmount()
            );
            productOptionPersistencePort.save(option);
        }
    }

    private void validateZeroPriceOptionAfterChange(ProductOption changed) {
        ProductOptionGroup group = productOptionGroupPersistencePort
            .findById(changed.getOptionGroupId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        if (!group.isRequired()) {
            return;
        }

        List<ProductOption> options = productOptionPersistencePort
            .findAllByOptionGroupId(changed.getOptionGroupId()).stream()
            .map(option -> option.getId().equals(changed.getId()) ? changed : option)
            .toList();
        ProductOptionSelectionRule.validateZeroPriceOption(group, options);
    }

    private Integer nextSort(Long optionGroupId) {
        return productOptionPersistencePort.findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).size();
    }

    private List<Long> distinct(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        return List.copyOf(unique);
    }
}
