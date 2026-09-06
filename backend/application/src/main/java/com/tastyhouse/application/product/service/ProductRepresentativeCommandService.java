package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductRepresentativeClearCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRequestCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductRepresentativeCommandService implements ProductRepresentativeCommandUseCase {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductRepresentativeCommandService(
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
