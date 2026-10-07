package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductExposureClearCommand;
import com.tastyhouse.application.product.port.in.ProductExposureClearUseCase;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductExposureClearService implements ProductExposureClearUseCase {

    private final ProductExposureService productExposureService;
    private final ProductPersistencePort productPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureClearService(
        ProductExposureService productExposureService,
        ProductPersistencePort productPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.productPersistencePort = productPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void clearExposure(ProductExposureClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productExposureService.clearSchedule(ProductId.of(productId));
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productPersistencePort.findAllByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
