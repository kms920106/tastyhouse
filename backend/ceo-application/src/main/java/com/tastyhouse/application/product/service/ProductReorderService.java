package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductReorderCommand;
import com.tastyhouse.application.product.port.in.ProductReorderUseCase;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductReorderService implements ProductReorderUseCase {

    private final ProductSortService productSortService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductReorderService(
        ProductSortService productSortService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productSortService = productSortService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void reorderProducts(ProductReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.reorderProducts(
            ShopId.of(shopId),
            toProductCategoryId(productCategoryId),
            toProductIds(productIds)
        );
    }

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }
        return productIds.stream().map(ProductId::of).toList();
    }
}
