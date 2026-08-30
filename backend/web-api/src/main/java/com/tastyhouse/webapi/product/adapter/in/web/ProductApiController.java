package com.tastyhouse.webapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductBatchRequest;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductDetailSearchRequest;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductSearchRequest;
import com.tastyhouse.webapplication.product.response.ProductBatchResponse;
import com.tastyhouse.webapplication.product.response.ProductDetailResponse;
import com.tastyhouse.webapplication.product.response.ProductImagesResponse;
import com.tastyhouse.webapplication.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewsByRatingPageResponse;
import com.tastyhouse.webapplication.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapplication.product.response.ProductTodayDiscountListItemResponse;
import com.tastyhouse.webapplication.product.port.in.ProductQueryUseCase;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "상품 관리 API")
public class ProductApiController {

    private final ProductQueryUseCase productQueryService;

    public ProductApiController(ProductQueryUseCase productQueryService) {
        this.productQueryService = productQueryService;
    }

    @Operation(summary = "상품 목록 조회 (오늘의 할인)", description = "할인율 기준으로 오늘의 할인 상품을 페이징하여 조회합니다. 상품명, 이미지, 원가, 할인가, 할인율 정보를 포함합니다.")
    @GetMapping("/v1/today-discounts")
    public ResponseEntity<ApiResponse<List<ProductTodayDiscountListItemResponse>>> getTodayDiscounts(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<ProductTodayDiscountListItemResponse> pageResponse = productQueryService.searchTodayDiscountProducts(pageRequest.page(), pageRequest.size());
        ApiResponse<List<ProductTodayDiscountListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회",
        description = "상품의 기본 정보와 가격명별 가격 목록(prices)을 조회합니다. prices의 각 price는 "
            + "orderMethod로 서버가 이미 해석한 단일 결제 가격이므로 화면이 배달가/픽업가를 고르지 않습니다. "
            + "orderMethod를 생략하면 DELIVERY로 조회합니다. 가격 행이 없는 메뉴(이관 이전 데이터)는 prices가 "
            + "빈 배열이며 기존 originalPrice/discountPrice 필드는 그대로 동작합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
        @PathVariable Long id,
        @Valid @ModelAttribute ProductDetailSearchRequest search
    ) {
        ProductDetailResponse response = productQueryService.findProductById(id, search.orderMethod());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 배치 조회 (옵션 포함)", description = "여러 (상품ID, 옵션ID) 조합을 한 번에 조회합니다. 상품별로 그룹핑하여 기본 정보(이름/정가/할인가)와 요청한 옵션 정보만 반환하므로, 상품마다 상세·옵션 API를 따로 호출하는 N+1 호출을 단일 호출로 대체합니다. 장바구니·주문서 등 여러 상품을 동시에 표시하는 화면에서 사용합니다. 판매 종료되었거나 존재하지 않는 상품은 결과에서 제외하지 않고 available=false 로 남깁니다(요청 순서 유지). 요청한 옵션 중 조회에 실패하거나 해당 상품에 속하지 않는 옵션은 options 에서 제외됩니다.")
    @PostMapping("/v1/batch")
    public ResponseEntity<ApiResponse<ProductBatchResponse>> getProductsBatch(@Valid @RequestBody ProductBatchRequest request) {
        ProductBatchResponse response = productQueryService.findProductsBatch(request.toQuery());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 이미지 목록 조회", description = "상품의 이미지 URL 목록을 조회합니다.")
    @GetMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<ProductImagesResponse>> getProductImages(@PathVariable Long id) {
        ProductImagesResponse response = productQueryService.findProductImages(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 옵션 조회", description = "상품의 옵션 그룹 및 옵션 목록을 조회합니다. 개별 옵션(isCommon: false)과 공통 옵션(isCommon: true)을 단일 목록으로 반환합니다.")
    @GetMapping("/v1/{id}/options")
    public ResponseEntity<ApiResponse<ProductOptionGroupsResponse>> getProductOptions(@PathVariable Long id) {
        ProductOptionGroupsResponse response = productQueryService.findProductOptions(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 리뷰 수 조회", description = "상품의 리뷰 총 개수를 조회합니다.")
    @GetMapping("/v1/{id}/reviews/count")
    public ResponseEntity<ApiResponse<ProductReviewCountResponse>> getProductReviewCount(@PathVariable Long id) {
        ProductReviewCountResponse response = productQueryService.findProductReviewCount(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 리뷰 목록 조회", description = "상품의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점, 2점, 3점, 4점, 5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다. 총 리뷰 개수도 함께 반환됩니다.")
    @GetMapping("/v1/{id}/reviews")
    public ResponseEntity<ApiResponse<ProductReviewsByRatingResponse>> getProductReviews(
        @PathVariable Long id,
        @Valid @ModelAttribute ProductSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        ProductReviewsByRatingPageResponse result = productQueryService.getProductReviewsByRatingWithPagination(id, pageRequest.page(), pageRequest.size(), search.hasImage());
        ApiResponse<ProductReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 리뷰 통계 조회", description = "상품의 리뷰 통계를 조회합니다. 평점, 맛 평점, 양 평점, 가격 평점을 포함합니다.")
    @GetMapping("/v1/{id}/reviews/statistics")
    public ResponseEntity<ApiResponse<ProductReviewStatisticsResponse>> getProductReviewStatistics(@PathVariable Long id) {
        ProductReviewStatisticsResponse statistics = productQueryService.getProductReviewStatistics(id);
        ApiResponse<ProductReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }
}
