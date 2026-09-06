package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.CupDepositOptionRule;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductOptionSelectionRule;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;

@Service
@CeoApp
@Transactional
public class ProductOptionCommandService implements ProductOptionCommandUseCase {

    private static final boolean DEFAULT_SOLD_OUT = false;
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionCommandService(
        ProductRegistrationService productRegistrationService,
        ProductOptionRepository productOptionRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        CupDepositPolicy cupDepositPolicy,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionRepository = productOptionRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
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

        productOptionRepository.save(option);
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
            productOptionRepository.findAllByOptionGroupId(group.getProductOptionGroupId());

        ProductOptionSelectionRule.validateRemainingAfterBlocking(group, option, groupOptions);

        option.hide();

        List<ProductOption> groupOptionsAfterHide = groupOptions.stream()
            .map(candidate -> candidate.getId().equals(option.getId()) ? option : candidate)
            .toList();
        ProductOptionSelectionRule.validateZeroPriceOption(group, groupOptionsAfterHide);

        productOptionRepository.save(option);
    }

    @Override
    public void changeProductOptionOrder(ProductOptionOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long optionGroupId = command.optionGroupId();
        List<Long> optionIds = command.optionIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        Map<Long, ProductOption> byId = productOptionRepository
            .findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        List<Long> requested = distinct(optionIds);
        if (byId.size() != requested.size() || !byId.keySet().containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
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
            productOptionRepository.save(option);
        }
    }

    private void validateZeroPriceOptionAfterChange(ProductOption changed) {
        ProductOptionGroup group = productOptionGroupRepository
            .findById(changed.getOptionGroupId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        if (!group.isRequired()) {
            return;
        }

        List<ProductOption> options = productOptionRepository
            .findAllByOptionGroupId(changed.getOptionGroupId()).stream()
            .map(option -> option.getId().equals(changed.getId()) ? changed : option)
            .toList();
        ProductOptionSelectionRule.validateZeroPriceOption(group, options);
    }

    private Integer nextSort(Long optionGroupId) {
        return productOptionRepository.findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).size();
    }

    private List<Long> distinct(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        return List.copyOf(unique);
    }
}
