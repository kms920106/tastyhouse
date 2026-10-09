package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.application.product.port.in.ProductOptionGroupListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductOptionGroupListQueryService implements ProductOptionGroupListQueryUseCase {

    private final ProductOptionGroupQueryPort productOptionGroupQueryPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupListQueryService(
        ProductOptionGroupQueryPort productOptionGroupQueryPort,
        CupDepositPolicy cupDepositPolicy,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOptionGroupQueryPort = productOptionGroupQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productOptionGroupQueryPort.findProductOptionGroupsForManagement(shopId).stream()
            .map(this::toOptionGroupViewResult)
            .toList();
    }

    private ProductOptionGroupViewResult toOptionGroupViewResult(ProductOptionGroupManagementResult row) {
        return new ProductOptionGroupViewResult(
            row.id(),
            row.name(),
            row.description(),
            row.required(),
            row.multipleSelect(),
            row.minSelect(),
            row.maxSelect(),
            row.sort(),
            row.visible(),
            row.groupType(),
            row.linkedProductCount(),
            row.options().stream().map(this::toOptionView).toList()
        );
    }

    private ProductOptionGroupViewResult.Option toOptionView(ProductOptionManagementResult row) {
        return new ProductOptionGroupViewResult.Option(
            row.id(),
            row.name(),
            row.additionalPrice(),
            row.sort(),
            row.visible(),
            row.cupCount(),

            cupDepositPolicy.depositAmountOf(row.cupCount()),
            row.personalCupDiscountAmount()
        );
    }
}
