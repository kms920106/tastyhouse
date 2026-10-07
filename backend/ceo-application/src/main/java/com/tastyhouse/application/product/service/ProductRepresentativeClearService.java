package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductRepresentativeClearCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeClearUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductRepresentativeClearService implements ProductRepresentativeClearUseCase {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductRepresentativeClearService(
        ProductRepresentativeApprovalService productRepresentativeApprovalService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void clearRepresentative(ProductRepresentativeClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ProductId targetProductId = ProductId.of(productId);

        productRepresentativeApprovalService.clearRepresentative(targetShopId, targetProductId);
    }
}
