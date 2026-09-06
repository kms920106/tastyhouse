package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;
import com.tastyhouse.application.product.port.in.ProductOptionGroupQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductOptionGroupQueryService implements ProductOptionGroupQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        CupDepositPolicy cupDepositPolicy,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productOwnerQueryPort.findProductOptionGroupsForManagement(shopId).stream()
            .map(this::toOptionGroupViewResult)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLinkedProductResult> getLinkedProducts(
        Long ceoId,
        Long shopId,
        Long optionGroupId
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductOptionGroupLinkedProductResult> linked =
            productOwnerQueryPort.findLinkedProductsByOptionGroupId(optionGroupId);
        boolean ownedByRequestedShop = linked.stream().anyMatch(row -> shopId.equals(row.shopId()));
        if (!ownedByRequestedShop) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }

        return linked.stream()
            .filter(row -> shopId.equals(row.shopId()))
            .toList();
    }

    @Override
    public List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productOwnerQueryPort.findLinkedProductsByShop(shopId);

        return linkedByGroupId.entrySet().stream()
            .map(entry -> new ProductOptionGroupLinkedProductsResult(entry.getKey(), entry.getValue()))
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
