package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ProductManagementQueryPort {

    PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery);

    PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(ApprovalStatus status, PageQuery pageQuery);

    PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    ProductOptionsResult findProductOptions(Long productId);

    List<String> findProductImageUrls(Long productId);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);
}
