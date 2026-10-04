package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductShopLinkQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductShopLinkQueryPort;
import com.tastyhouse.application.product.port.out.ProductShopLinkResult;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductShopLinkQueryService implements ProductShopLinkQueryUseCase {

    private final ProductShopLinkQueryPort productShopLinkQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkQueryService(
        ProductShopLinkQueryPort productShopLinkQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkQueryPort = productShopLinkQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductShopLinkResult> getShopLinks(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productShopLinkQueryPort.findOwnedShopLinks(ceoId, productId);
    }

}
