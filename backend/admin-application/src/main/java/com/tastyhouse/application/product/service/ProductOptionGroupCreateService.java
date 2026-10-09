package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositOptionRule;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.product.port.in.ProductOptionGroupCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupManagementCreateCommand;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;

@Service
@Transactional
class ProductOptionGroupCreateService implements ProductOptionGroupCreateUseCase {

    private final ProductRegistrationService productRegistrationService;
    private final ProductLoadPort productLoadPort;
    private final ShopLoadPort shopLoadPort;

    public ProductOptionGroupCreateService(
        ProductRegistrationService productRegistrationService,
        ProductLoadPort productLoadPort,
        ShopLoadPort shopLoadPort
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productLoadPort = productLoadPort;
        this.shopLoadPort = shopLoadPort;
    }

    @Override
    public Long createProductOptionGroup(ProductOptionGroupManagementCreateCommand command) {
        Long id = command.productId();
        String name = command.name();
        String description = command.description();
        boolean required = command.required();
        boolean multipleSelect = command.multipleSelect();
        Integer minSelect = command.minSelect();
        Integer maxSelect = command.maxSelect();
        Integer sort = command.sort();
        boolean visible = command.visible();
        String groupType = command.groupType();

        ProductOptionGroupType resolvedGroupType = ProductOptionGroupType.from(groupType);

        if (resolvedGroupType.isCupDeposit()) {
            loadShopOf(ProductId.of(id)).validateCupDepositEnabled();
        }
        CupDepositOptionRule.validateDepositGroupSelectRange(
            resolvedGroupType, required, multipleSelect, minSelect, maxSelect
        );

        ProductOptionGroup optionGroup = productRegistrationService.saveProductOptionGroup(
            ProductId.of(id),
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            sort,
            visible,
            resolvedGroupType
        );
        return optionGroup.getId();
    }

    private Shop loadShopOf(ProductId productId) {
        Product product = productLoadPort.findActiveById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        return shopLoadPort.findById(product.getShopId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }
}
