package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductBatchItemView;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailView;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsView;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductQueryUseCase {

    PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size);

    ProductDetailView findProductById(Long productId, String orderMethod);

    int findProductReviewCount(Long productId);

    ProductOptionsResult findProductOptions(Long productId);

    List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query);

    List<String> findProductImages(Long productId);

    ReviewsByRatingResult getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);

    ProductReviewStatisticsView getProductReviewStatistics(Long productId);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size);

    List<ShopProductItemResult> findShopProducts(Long shopId);

    List<PopularProductItemResult> findPopularProducts(Long shopId);

    List<ProductCategoryResult> findShopProductCategories(Long shopId);
}
