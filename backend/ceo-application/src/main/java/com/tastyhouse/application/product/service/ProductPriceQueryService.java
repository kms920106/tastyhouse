package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductPriceQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductPriceQueryService implements ProductPriceQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductPriceQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOwnerPriceView> getPrices(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        boolean ownedProduct = productOwnerQueryPort.findExposurePeriod(productId)
            .map(period -> period.shopId().equals(shopId))
            .orElse(false);
        if (!ownedProduct) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }

        return productOwnerQueryPort.findPrices(productId);
    }

}
