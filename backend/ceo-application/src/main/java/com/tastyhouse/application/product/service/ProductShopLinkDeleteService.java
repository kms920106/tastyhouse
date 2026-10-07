package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductShopLinkDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkDeleteUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductShopLinkDeleteService implements ProductShopLinkDeleteUseCase {

    private final ProductShopLinkService productShopLinkService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkDeleteService(
        ProductShopLinkService productShopLinkService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkService = productShopLinkService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void unlinkFromShop(ProductShopLinkDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long productId = command.productId();
        Long targetShopId = command.targetShopId();

        shopOwnershipValidator.validateOwnership(ceoId, targetShopId);

        productShopLinkService.unlinkFromShop(ProductId.of(productId), ShopId.of(targetShopId));
    }
}
