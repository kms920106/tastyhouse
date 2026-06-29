package com.tastyhouse.webapi.product;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.product.request.ProductBatchRequest;
import com.tastyhouse.webapi.product.response.ProductBatchResponse;
import com.tastyhouse.webapi.product.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingWithPagination;
import com.tastyhouse.webapi.product.response.TodayDiscountProductListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "상품 배치 조회 (옵션 포함)", description = "여러 (상품ID, 옵션ID) 조합을 한 번에 조회합니다. 상품별로 그룹핑하여 기본 정보(이름/정가/할인가)와 요청한 옵션 정보만 반환하므로, 상품마다 상세·옵션 API를 따로 호출하는 N+1 호출을 단일 호출로 대체합니다. 장바구니·주문서 등 여러 상품을 동시에 표시하는 화면에서 사용합니다. 판매 종료되었거나 존재하지 않는 상품은 결과에서 제외하지 않고 available=false 로 남깁니다(요청 순서 유지). 요청한 옵션 중 조회에 실패하거나 해당 상품에 속하지 않는 옵션은 options 에서 제외됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PostMapping("/v1/batch")
    public ResponseEntity<ApiResponse<ProductBatchResponse>> getProductsBatch(@Valid @RequestBody ProductBatchRequest request) {
        ProductBatchResponse response = productService.findProductsBatch(request);
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
        @Valid @ModelAttribute PageRequest pageRequest,
        @Parameter(description = "이미지 유무 필터: 미지정=전체, true=이미지 있는 리뷰, false=이미지 없는 리뷰")
        @RequestParam(required = false) Boolean hasImage
    ) {
        ProductReviewsByRatingWithPagination result = productService.getProductReviewsByRatingWithPagination(productId, pageRequest.page(), pageRequest.size(), hasImage);
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
