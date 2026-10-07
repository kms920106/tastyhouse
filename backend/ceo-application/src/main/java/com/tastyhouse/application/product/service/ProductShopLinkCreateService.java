package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductShopLinkCreateCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkCreateUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductShopLinkCreateService implements ProductShopLinkCreateUseCase {

    private final ProductShopLinkService productShopLinkService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkCreateService(
        ProductShopLinkService productShopLinkService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkService = productShopLinkService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void linkToShop(ProductShopLinkCreateCommand command) {
        Long ceoId = command.ceoId();
        Long productId = command.productId();
        Long targetShopId = command.targetShopId();
        Long productCategoryId = command.productCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, targetShopId);

        productShopLinkService.linkToShop(
            ProductId.of(productId), ShopId.of(targetShopId), productCategoryId
        );
    }
}
