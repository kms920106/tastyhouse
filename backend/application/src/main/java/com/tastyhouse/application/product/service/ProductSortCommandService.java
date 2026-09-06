package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductCategoryReorderCommand;
import com.tastyhouse.application.product.port.in.ProductRelocateCommand;
import com.tastyhouse.application.product.port.in.ProductReorderCommand;
import com.tastyhouse.application.product.port.in.ProductSortCommandUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.service.ProductSortService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductSortCommandService implements ProductSortCommandUseCase {

    private final ProductSortService productSortService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductSortCommandService(
        ProductSortService productSortService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productSortService = productSortService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void reorderProductCategories(ProductCategoryReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productCategoryIds = command.productCategoryIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.reorderCategories(ShopId.of(shopId), toProductCategoryIds(productCategoryIds));
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

    private List<ProductCategoryId> toProductCategoryIds(List<Long> productCategoryIds) {
        if (productCategoryIds == null || productCategoryIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);
        }
        return productCategoryIds.stream().map(ProductCategoryId::of).toList();
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }
        return productIds.stream().map(ProductId::of).toList();
    }
}
