package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductPriceCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductPriceItemCommand;
import com.tastyhouse.application.product.port.in.ProductPriceReplaceCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductPriceService;
import com.tastyhouse.domain.product.service.ProductPriceSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductPriceCommandService implements ProductPriceCommandUseCase {

    private final ProductPriceService productPriceService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductPriceCommandService(
        ProductPriceService productPriceService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productPriceService = productPriceService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void replacePrices(ProductPriceReplaceCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<ProductPriceItemCommand> prices = command.prices();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ProductId targetProductId = ProductId.of(productId);
        List<ProductPriceSpec> specs = prices.stream().map(this::toProductPriceSpec).toList();
        productPriceService.replacePrices(targetShopId, targetProductId, specs, LocalDateTime.now());
    }

    private ProductPriceSpec toProductPriceSpec(ProductPriceItemCommand item) {
        return ProductPriceSpec.of(
            item.priceId(),
            item.priceName(),
            item.deliveryPrice(),
            item.storePrice(),
            item.pickupPrice(),
            item.sort()
        );
    }
}
