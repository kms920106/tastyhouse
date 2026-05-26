package com.tastyhouse.webapi.product;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.product.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingWithPagination;
import com.tastyhouse.webapi.product.response.TodayDiscountProductListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관리 API")
public class ProductApiController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회 (오늘의 할인)", description = "할인율 기준으로 오늘의 할인 상품을 페이징하여 조회합니다. 상품명, 이미지, 원가, 할인가, 할인율 정보를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/today-discounts")
    public ResponseEntity<ApiResponse<List<TodayDiscountProductListItemResponse>>> getTodayDiscounts(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResponse<TodayDiscountProductListItemResponse> pageResult = productService.searchTodayDiscountProducts(pageRequest.page(), pageRequest.size());
        ApiResponse<List<TodayDiscountProductListItemResponse>> response = ApiResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회", description = "상품의 기본 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/v1/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long productId) {
        ProductDetailResponse response = productService.findProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 이미지 목록 조회", description = "상품의 이미지 URL 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/v1/{productId}/images")
    public ResponseEntity<ApiResponse<ProductImagesResponse>> getProductImages(@PathVariable Long productId) {
        ProductImagesResponse response = productService.findProductImages(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 옵션 조회", description = "상품의 옵션 그룹 및 옵션 목록을 조회합니다. 개별 옵션(isCommon: false)과 공통 옵션(isCommon: true)을 단일 목록으로 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/v1/{productId}/options")
    public ResponseEntity<ApiResponse<ProductOptionGroupsResponse>> getProductOptions(@PathVariable Long productId) {
        ProductOptionGroupsResponse response = productService.findProductOptions(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 리뷰 수 조회", description = "상품의 리뷰 총 개수를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/v1/{productId}/reviews/count")
    public ResponseEntity<ApiResponse<ProductReviewCountResponse>> getProductReviewCount(@PathVariable Long productId) {
        ProductReviewCountResponse response = productService.findProductReviewCount(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 리뷰 목록 조회", description = "상품의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점, 2점, 3점, 4점, 5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다. 총 리뷰 개수도 함께 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{productId}/reviews")
    public ResponseEntity<ApiResponse<ProductReviewsByRatingResponse>> getProductReviews(
            @PathVariable Long productId,
            @Valid @ModelAttribute PageRequest pageRequest) {
        ProductReviewsByRatingWithPagination result = productService.getProductReviewsByRatingWithPagination(productId, pageRequest.page(), pageRequest.size());
        ApiResponse<ProductReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 리뷰 통계 조회", description = "상품의 리뷰 통계를 조회합니다. 평점, 맛 평점, 양 평점, 가격 평점을 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{productId}/reviews/statistics")
    public ResponseEntity<ApiResponse<ProductReviewStatisticsResponse>> getProductReviewStatistics(@PathVariable Long productId) {
        ProductReviewStatisticsResponse statistics = productService.getProductReviewStatistics(productId);
        ApiResponse<ProductReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }
}
