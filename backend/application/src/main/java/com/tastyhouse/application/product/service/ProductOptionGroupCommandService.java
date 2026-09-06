package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.CupDepositOptionRule;
import com.tastyhouse.domain.product.service.ProductOptionSelectionRule;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductOptionGroupCommandService implements ProductOptionGroupCommandUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private static final Integer NEXT_SORT_APPENDS_TO_TAIL = null;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupCommandService(
        ProductRegistrationService productRegistrationService,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductRepository productRepository,
        ShopRepository shopRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
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
            productOptionRepository.findAllByOptionGroupId(group.getProductOptionGroupId());
        ProductOptionSelectionRule.validateZeroPriceOption(group, groupOptions);

        productOptionGroupRepository.save(group);
    }

    @Override
    public void deleteProductOptionGroup(ProductOptionGroupDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long optionGroupId = command.optionGroupId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
        group.hide();
        productOptionGroupRepository.save(group);
    }

    private Shop loadShop(Long shopId) {
        return shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private void validateSelectRange(Integer minSelect, Integer maxSelect) {
        if (minSelect != null && maxSelect != null && minSelect > maxSelect) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_SELECT_RANGE_INVALID);
        }
    }
}
