package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.product.port.out.ProductBatchItemView;
import com.tastyhouse.application.product.port.out.ProductDetailView;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsView;

@WebApp
public interface ProductQueryUseCase {

    PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size);

    ProductDetailView findProductById(Long productId, String orderMethod);

    int findProductReviewCount(Long productId);

    ProductOptionsResult findProductOptions(Long productId);

    List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query);

    List<String> findProductImages(Long productId);

    ReviewsByRatingResult getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);

    ProductReviewStatisticsView getProductReviewStatistics(Long productId);
}
