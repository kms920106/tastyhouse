package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkState;

final class ProductShopLinkStateMapper {
    private ProductShopLinkStateMapper() {
    }

    static ProductShopLink toDomain(ProductShopLinkState state) {
        return ProductShopLink.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.productCategoryId() == null ? null : ProductCategoryId.of(state.productCategoryId()),
            state.sort()
        );
    }

    static ProductShopLinkState toState(ProductShopLink link) {
        return new ProductShopLinkState(
            link.getId(),
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getShopId() == null ? null : link.getShopId().value(),
            link.getProductCategoryId() == null ? null : link.getProductCategoryId().value(),
            link.getSort()
        );
    }
}
