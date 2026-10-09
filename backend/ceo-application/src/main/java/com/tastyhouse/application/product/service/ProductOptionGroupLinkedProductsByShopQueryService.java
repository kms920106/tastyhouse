package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkedProductsByShopQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupQueryPort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductOptionGroupLinkedProductsByShopQueryService implements ProductOptionGroupLinkedProductsByShopQueryUseCase {

    private final ProductOptionGroupQueryPort productOptionGroupQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupLinkedProductsByShopQueryService(
        ProductOptionGroupQueryPort productOptionGroupQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOptionGroupQueryPort = productOptionGroupQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productOptionGroupQueryPort.findLinkedProductsByShop(shopId);

        return linkedByGroupId.entrySet().stream()
            .map(entry -> new ProductOptionGroupLinkedProductsResult(entry.getKey(), entry.getValue()))
            .toList();
    }
}
