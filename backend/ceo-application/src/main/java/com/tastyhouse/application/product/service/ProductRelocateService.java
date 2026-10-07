package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductRelocateCommand;
import com.tastyhouse.application.product.port.in.ProductRelocateUseCase;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductRelocateService implements ProductRelocateUseCase {

    private final ProductSortService productSortService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductRelocateService(
        ProductSortService productSortService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productSortService = productSortService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void relocateProducts(ProductRelocateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long targetProductCategoryId = command.targetProductCategoryId();
        List<Long> productIds = command.productIds();
        List<Long> targetOrderedProductIds = command.targetOrderedProductIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.relocateProducts(
            ShopId.of(shopId),
            toProductCategoryId(targetProductCategoryId),
            toProductIds(productIds),
            toProductIds(targetOrderedProductIds)
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
