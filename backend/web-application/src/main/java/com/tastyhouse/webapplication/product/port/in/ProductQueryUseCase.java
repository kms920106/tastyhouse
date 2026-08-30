package com.tastyhouse.webapplication.product.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.product.response.ProductBatchResponse;
import com.tastyhouse.webapplication.product.response.ProductDetailResponse;
import com.tastyhouse.webapplication.product.response.ProductImagesResponse;
import com.tastyhouse.webapplication.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewsByRatingPageResponse;
import com.tastyhouse.webapplication.product.response.ProductTodayDiscountListItemResponse;

/**
 * 상품 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductQueryUseCase {

    PaginationResponse<ProductTodayDiscountListItemResponse> searchTodayDiscountProducts(int page, int size);

    ProductDetailResponse findProductById(Long productId, String orderMethod);

    ProductReviewCountResponse findProductReviewCount(Long productId);

    ProductOptionGroupsResponse findProductOptions(Long productId);

    ProductBatchResponse findProductsBatch(ProductBatchQuery query);

    ProductImagesResponse findProductImages(Long productId);

    ProductReviewsByRatingPageResponse getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);

    ProductReviewStatisticsResponse getProductReviewStatistics(Long productId);
}
