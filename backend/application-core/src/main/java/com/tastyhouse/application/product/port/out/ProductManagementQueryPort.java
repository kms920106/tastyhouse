package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductManagementQueryPort {

    PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery);

    List<String> findProductImageUrls(Long productId);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);
}
