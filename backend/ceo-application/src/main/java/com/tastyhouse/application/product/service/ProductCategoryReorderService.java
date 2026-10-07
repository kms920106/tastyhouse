package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductCategoryReorderCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryReorderUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductCategoryReorderService implements ProductCategoryReorderUseCase {

    private final ProductSortService productSortService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryReorderService(
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

    private List<ProductCategoryId> toProductCategoryIds(List<Long> productCategoryIds) {
        if (productCategoryIds == null || productCategoryIds.isEmpty()) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);
        }
        return productCategoryIds.stream().map(ProductCategoryId::of).toList();
    }
}
