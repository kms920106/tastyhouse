package com.tastyhouse.webapi.product;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.product.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductService productService;

    @Operation(summary = "오늘의 할인 상품 목록 조회", description = "할인율 기준으로 오늘의 할인 상품을 페이징하여 조회합니다. 상품명, 이미지, 원가, 할인가, 할인율 정보를 포함합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/today-discounts")
    public ResponseEntity<CommonResponse<List<TodayDiscountProductItem>>> getTodayDiscounts(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<TodayDiscountProductItem> pageResult = productService.searchTodayDiscountProducts(pageRequest);
        CommonResponse<List<TodayDiscountProductItem>> response = CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플레이스별 상품 목록 조회", description = "특정 플레이스의 상품 목록을 조회합니다. 상품명, 이미지, 가격, 할인 정보, 평점, 리뷰 수 등을 포함합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/places/{placeId}")
    public ResponseEntity<CommonResponse<List<ProductListItem>>> getProductsByPlaceId(@PathVariable Long placeId) {
        List<ProductListItem> products = productService.searchProductsByPlaceId(placeId);
        CommonResponse<List<ProductListItem>> response = CommonResponse.success(products);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플레이스별 상품 카테고리 목록 조회", description = "특정 플레이스의 상품 카테고리 목록을 조회합니다. 카테고리 타입, 표시명, 정렬 순서를 포함합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/places/{placeId}/categories")
    public ResponseEntity<CommonResponse<List<ProductCategoryListItem>>> getProductCategoriesByPlaceId(@PathVariable Long placeId) {
        List<ProductCategoryListItem> categories = productService.searchProductCategoriesByPlaceId(placeId);
        CommonResponse<List<ProductCategoryListItem>> response = CommonResponse.success(categories);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 상세 조회", description = "상품의 상세 정보를 조회합니다. 기본 정보와 함께 옵션 그룹 및 옵션 목록을 포함합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/v1/{productId}")
    public ResponseEntity<CommonResponse<ProductDetailResponse>> getProductById(@PathVariable Long productId) {
        return productService.findProductById(productId)
            .map(product -> ResponseEntity.ok(CommonResponse.success(product)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 목록 조회", description = "상품의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점, 2점, 3점, 4점, 5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다. 총 리뷰 개수도 함께 반환됩니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/{productId}/reviews")
    public ResponseEntity<CommonResponse<ProductReviewsByRatingResponse>> getProductReviews(
            @PathVariable Long productId,
            @Valid @ModelAttribute PageRequest pageRequest) {
        ProductReviewsByRatingWithPagination result = productService.getProductReviewsByRatingWithPagination(productId, pageRequest.page(), pageRequest.size());
        CommonResponse<ProductReviewsByRatingResponse> response = CommonResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 통계 조회", description = "상품의 리뷰 통계를 조회합니다. 평점, 맛 평점, 양 평점, 가격 평점을 포함합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/{productId}/reviews/statistics")
    public ResponseEntity<CommonResponse<ProductReviewStatisticsResponse>> getProductReviewStatistics(@PathVariable Long productId) {
        ProductReviewStatisticsResponse statistics = productService.getProductReviewStatistics(productId);
        CommonResponse<ProductReviewStatisticsResponse> response = CommonResponse.success(statistics);
        return ResponseEntity.ok(response);
    }
}
