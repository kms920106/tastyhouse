package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;
import com.tastyhouse.application.product.port.in.ProductPriceQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductPriceService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductPriceQueryService implements ProductPriceQueryUseCase {

    private final ProductPriceService productPriceService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductPriceQueryService(
        ProductPriceService productPriceService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productPriceService = productPriceService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOwnerPriceView> getPrices(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productPriceService.findPrices(ShopId.of(shopId), ProductId.of(productId)).stream()
            .map(price -> new ProductOwnerPriceView(
                price.getId(),
                price.getPriceName(),
                price.getDeliveryPrice(),
                price.getStorePrice(),
                price.getPickupPrice(),
                price.getSort()
            ))
            .toList();
    }

}
