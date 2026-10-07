package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.application.product.port.in.ProductOptionGroupDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupDeleteUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupDeleteService implements ProductOptionGroupDeleteUseCase {

    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupDeleteService(
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void deleteProductOptionGroup(ProductOptionGroupDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long optionGroupId = command.optionGroupId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
        group.hide();
        productOptionGroupPersistencePort.save(group);
    }
}
