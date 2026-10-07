package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupOrderChangeService implements ProductOptionGroupOrderChangeUseCase {

    private final ProductOptionGroupLinkService productOptionGroupLinkService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupOrderChangeService(
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductPersistencePort productPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOptionGroupLinkService = productOptionGroupLinkService;
        this.productPersistencePort = productPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void changeOptionGroupOrder(ProductOptionGroupOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);

        productOptionGroupLinkService.reorder(ProductId.of(productId), toOptionGroupIds(optionGroupIds));
    }

    private void loadOwnedProduct(Long shopId, Long productId) {
        Product product = productPersistencePort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<ProductOptionGroupId> toOptionGroupIds(List<Long> optionGroupIds) {
        return optionGroupIds.stream().map(ProductOptionGroupId::of).toList();
    }
}
