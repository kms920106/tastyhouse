package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupUnlinkService implements ProductOptionGroupUnlinkUseCase {

    private final ProductOptionGroupLinkService productOptionGroupLinkService;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupUnlinkService(
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupLinkService = productOptionGroupLinkService;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void unlinkOptionGroup(ProductOptionGroupUnlinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.unlink(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    private void loadOwnedProduct(Long shopId, Long productId) {
        Product product = productLoadPort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
