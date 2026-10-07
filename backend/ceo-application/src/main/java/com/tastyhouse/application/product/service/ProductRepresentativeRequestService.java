package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRequestCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRequestUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductRepresentativeRequestService implements ProductRepresentativeRequestUseCase {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductRepresentativeRequestService(
        ProductRepresentativeApprovalService productRepresentativeApprovalService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<Long> requestRepresentative(ProductRepresentativeRequestCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        List<ProductId> targetProductIds = productIds.stream()
            .filter(java.util.Objects::nonNull)
            .map(ProductId::of)
            .toList();

        return productRepresentativeApprovalService.requestRepresentative(targetShopId, targetProductIds);
    }
}
