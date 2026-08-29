package com.tastyhouse.webapi.product.application.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductBatchRequest;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductBatchResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductReviewsByRatingPageResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductTodayDiscountListItemResponse;

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

    ProductBatchResponse findProductsBatch(ProductBatchRequest request);

    ProductImagesResponse findProductImages(Long productId);

    ProductReviewsByRatingPageResponse getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);

    ProductReviewStatisticsResponse getProductReviewStatistics(Long productId);
}
