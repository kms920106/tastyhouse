package com.tastyhouse.webapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.request.ShopReviewSearchRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopDetailResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopInfoResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopNoticeResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopPopularProductResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopProductCategoryResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopReviewsByRatingPageResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopReviewsByRatingResponse;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop Detail", description = "가게 상세 조회 API")
public class ShopDetailApiController {

    private final ShopDetailQueryUseCase shopDetailQueryUseCase;

    public ShopDetailApiController(ShopDetailQueryUseCase shopDetailQueryUseCase) {
        this.shopDetailQueryUseCase = shopDetailQueryUseCase;
    }

    @Operation(summary = "가게 상세 조회", description = "가게의 기본 정보를 조회합니다. 상호명, 주소, 위도/경도, 평점, 전화번호, 썸네일 이미지를 포함합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShopDetail(@PathVariable Long id) {
        ShopDetailResponse shopDetail = ShopDetailResponse.from(shopDetailQueryUseCase.getShopDetail(id));
        return ResponseEntity.ok(ApiResponse.success(shopDetail));
    }

    @Operation(summary = "정보 조회", description = "가게의 기본 정보를 조회합니다. 운영시간, 전화번호 등을 포함합니다.")
    @GetMapping("/v1/{id}/info")
    public ResponseEntity<ApiResponse<ShopInfoResponse>> getShopInfo(@PathVariable Long id) {
        ShopInfoResponse shopInfo = ShopInfoResponse.from(shopDetailQueryUseCase.getShopInfo(id));
        ApiResponse<ShopInfoResponse> response = ApiResponse.success(shopInfo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "배너 이미지 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerResponse>>> getShopBanners(@PathVariable Long id) {
        List<ShopBannerResponse> banners = shopDetailQueryUseCase.getShopBanners(id).stream()
            .map(ShopBannerResponse::from)
            .toList();
        ApiResponse<List<ShopBannerResponse>> response = ApiResponse.success(banners);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "점주 공지 조회", description = "가게에 노출 중인 점주 공지 1건을 조회합니다. 노출 중인 공지가 없으면 data가 null입니다.")
    @GetMapping("/v1/{id}/notice")
    public ResponseEntity<ApiResponse<ShopNoticeResponse>> getShopNotice(@PathVariable Long id) {
        ShopNoticeResult noticeResult = shopDetailQueryUseCase.getShopNotice(id);
        ShopNoticeResponse notice = noticeResult == null ? null : ShopNoticeResponse.from(noticeResult);
        ApiResponse<ShopNoticeResponse> response = ApiResponse.success(notice);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 목록 조회", description = "가게의 상품 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @GetMapping("/v1/{id}/products")
    public ResponseEntity<ApiResponse<List<ShopProductCategoryResponse>>> getShopProducts(@PathVariable Long id) {
        List<ShopProductCategoryResponse> products = shopDetailQueryUseCase.getShopProducts(id).stream()
            .map(ShopProductCategoryResponse::from)
            .toList();
        ApiResponse<List<ShopProductCategoryResponse>> response = ApiResponse.success(products);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인기 메뉴 그룹 조회",
        description = "가게 상세 상단 '가장 인기 있는 메뉴' 그룹을 최대 5건 조회합니다. 사장님 추천 메뉴를 먼저 "
            + "채우고 남는 자리를 최근 30일 완료 주문의 판매량 순으로 채웁니다. 판매중지·숨김·미노출 메뉴는 제외됩니다. "
            + "인증이 필요하지 않습니다.")
    @GetMapping("/v1/{id}/popular-products")
    public ResponseEntity<ApiResponse<List<ShopPopularProductResponse>>> getPopularProducts(@PathVariable Long id) {
        List<ShopPopularProductResponse> popularProducts = shopDetailQueryUseCase.getPopularProducts(id).stream()
            .map(ShopPopularProductResponse::from)
            .toList();
        ApiResponse<List<ShopPopularProductResponse>> response = ApiResponse.success(popularProducts);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포토 목록 조회", description = "가게의 사진 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @GetMapping("/v1/{id}/photos")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getShopPhotos(@PathVariable Long id) {
        List<ShopPhotoCategoryResponse> photos = shopDetailQueryUseCase.getShopPhotos(id).stream()
            .map(ShopPhotoCategoryResponse::from)
            .toList();
        ApiResponse<List<ShopPhotoCategoryResponse>> response = ApiResponse.success(photos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 목록 조회", description = "가게의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점~5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다.")
    @GetMapping("/v1/{id}/reviews")
    public ResponseEntity<ApiResponse<ShopReviewsByRatingResponse>> getShopReviews(
        @PathVariable Long id,
        @Valid @ModelAttribute ShopReviewSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        ShopReviewsByRatingPageResponse result = ShopReviewsByRatingPageResponse.from(
            shopDetailQueryUseCase.getShopReviewsByRatingWithPagination(
                id,
                pageRequest.page(),
                pageRequest.size(),
                search.hasImage(),
                search.sortType()
            )
        );
        ApiResponse<ShopReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 통계 조회", description = "가게의 리뷰 통계를 조회합니다. 평점, 카테고리별 점수, 재방문의사 등을 포함합니다.")
    @GetMapping("/v1/{id}/reviews/statistics")
    public ResponseEntity<ApiResponse<ShopReviewStatisticsResponse>> getShopReviewStatistics(@PathVariable Long id) {
        ShopReviewStatisticsResponse statistics =
            ShopReviewStatisticsResponse.from(shopDetailQueryUseCase.getShopReviewStatistics(id));
        ApiResponse<ShopReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }
}
