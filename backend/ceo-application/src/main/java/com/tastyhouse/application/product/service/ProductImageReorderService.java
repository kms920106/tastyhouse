package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductImageReorderCommand;
import com.tastyhouse.application.product.port.in.ProductImageReorderUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductImageReorderService implements ProductImageReorderUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductImageReorderService(
        ProductImageApprovalService productImageApprovalService,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void reorderImages(ProductImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> imageIds = command.imageIds();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageApprovalService.reorderImages(ProductId.of(productId), imageIds);
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        if (notOwnedBy(shopId, ProductId.of(productId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productLoadPort.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
