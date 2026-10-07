package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerLinkUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupOwnerLinkService implements ProductOptionGroupOwnerLinkUseCase {

    private final ProductOptionGroupLinkService productOptionGroupLinkService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupOwnerLinkService(
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductPersistencePort productPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupLinkService = productOptionGroupLinkService;
        this.productPersistencePort = productPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void linkOptionGroup(ProductOptionGroupLinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.link(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    private void loadOwnedProduct(Long shopId, Long productId) {
        Product product = productPersistencePort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
