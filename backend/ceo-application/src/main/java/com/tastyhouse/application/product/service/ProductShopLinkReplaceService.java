package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductShopLinkItemCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkReplaceCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkReplaceUseCase;
import com.tastyhouse.application.shop.service.OwnedShopIdProvider;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductShopLinkReplaceService implements ProductShopLinkReplaceUseCase {

    private final ProductShopLinkService productShopLinkService;
    private final OwnedShopIdProvider ownedShopIdProvider;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkReplaceService(
        ProductShopLinkService productShopLinkService,
        OwnedShopIdProvider ownedShopIdProvider,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkService = productShopLinkService;
        this.ownedShopIdProvider = ownedShopIdProvider;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void replaceLinks(ProductShopLinkReplaceCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<ProductShopLinkItemCommand> links = command.links();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductShopLinkSpec> specs = links.stream().map(this::toProductShopLinkSpec).toList();
        productShopLinkService.replaceLinks(ProductId.of(productId), specs, ownedShopIds(ceoId));
    }

    private Set<Long> ownedShopIds(Long ceoId) {
        return ownedShopIdProvider.findOwnedShopIds(ceoId);
    }

    private ProductShopLinkSpec toProductShopLinkSpec(ProductShopLinkItemCommand item) {
        return ProductShopLinkSpec.of(item.shopId(), item.productCategoryId());
    }
}
