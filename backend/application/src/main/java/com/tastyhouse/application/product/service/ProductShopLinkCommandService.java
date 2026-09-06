package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductShopLinkCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductShopLinkCreateCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkItemCommand;
import com.tastyhouse.application.product.port.in.ProductShopLinkReplaceCommand;
import com.tastyhouse.application.shop.service.OwnedShopIdProvider;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductShopLinkService;
import com.tastyhouse.domain.product.service.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductShopLinkCommandService implements ProductShopLinkCommandUseCase {

    private final ProductShopLinkService productShopLinkService;
    private final OwnedShopIdProvider ownedShopIdProvider;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkCommandService(
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

    @Override
    public void unlinkFromShop(ProductShopLinkDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long productId = command.productId();
        Long targetShopId = command.targetShopId();

        shopOwnershipValidator.validateOwnership(ceoId, targetShopId);

        productShopLinkService.unlinkFromShop(ProductId.of(productId), ShopId.of(targetShopId));
    }

    private Set<Long> ownedShopIds(Long ceoId) {
        return ownedShopIdProvider.findOwnedShopIds(ceoId);
    }

    private ProductShopLinkSpec toProductShopLinkSpec(ProductShopLinkItemCommand item) {
        return ProductShopLinkSpec.of(item.shopId(), item.productCategoryId());
    }
}
