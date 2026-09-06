package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOwnerQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductOwnerQueryService implements ProductOwnerQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOwnerQueryService(ProductOwnerQueryPort productOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductManagementDetailResult getProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductManagementDetailResult dto = productOwnerQueryPort.findProductManagementDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!dto.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return dto;
    }

}
