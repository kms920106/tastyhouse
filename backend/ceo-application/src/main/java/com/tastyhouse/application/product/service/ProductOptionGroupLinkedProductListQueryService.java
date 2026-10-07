package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkedProductListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductOptionGroupLinkedProductListQueryService implements ProductOptionGroupLinkedProductListQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupLinkedProductListQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
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
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }

        return linked.stream()
            .filter(row -> shopId.equals(row.shopId()))
            .toList();
    }
}
