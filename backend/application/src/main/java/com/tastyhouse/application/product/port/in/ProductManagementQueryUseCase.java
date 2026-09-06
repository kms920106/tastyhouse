package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ProductManagementQueryUseCase {

    PageResult<ProductListItemResult> getProducts(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut,
        int page,
        int size
    );

    ProductDetailResult getProduct(Long id);

    ProductOptionsResult getProductOptions(Long id);

    List<String> getProductImages(Long id);

    List<ProductCategoryResult> getProductCategories(Long shopId);
}
