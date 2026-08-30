package com.tastyhouse.webapi.search.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.infrastructure.redis.ratelimit.RateLimit;
import com.tastyhouse.infrastructure.redis.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapplication.auth.security.CustomUserDetails;
import com.tastyhouse.webapplication.product.response.ProductSummaryResponse;
import com.tastyhouse.webapplication.search.port.in.SearchQueryUseCase;
import com.tastyhouse.webapi.search.adapter.in.web.request.SearchKeywordRequest;
import com.tastyhouse.webapplication.search.response.SearchPopularKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchRecommendedKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapplication.search.response.SearchShopListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "검색 API")
public class SearchApiController {

    private final SearchQueryUseCase searchQueryService;

    public SearchApiController(SearchQueryUseCase searchQueryService) {
        this.searchQueryService = searchQueryService;
    }

    @Operation(summary = "인기 검색어 조회", description = "1~10위 인기 검색어 반환. 신규 진입 키워드는 isNew=true.")
    @GetMapping("/v1/popular-keywords")
    public ResponseEntity<ApiResponse<List<SearchPopularKeywordResponse>>> getPopularKeywords() {
        return ResponseEntity.ok(ApiResponse.success(searchQueryService.getPopularKeywords()));
    }

    @Operation(summary = "추천 검색어 조회", description = "운영 관리 추천 검색어 태그 목록 반환.")
    @GetMapping("/v1/recommended-keywords")
    public ResponseEntity<ApiResponse<List<SearchRecommendedKeywordResponse>>> getRecommendedKeywords() {
        return ResponseEntity.ok(ApiResponse.success(searchQueryService.getRecommendedKeywords()));
    }

    @Operation(summary = "메뉴 검색", description = "메뉴 탭 — 메뉴명 기반 검색. 판매 중인 메뉴만 포함.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:menus")
    @GetMapping("/v1/menus")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> searchMenus(
        @Valid @ModelAttribute SearchKeywordRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var result = searchQueryService.searchMenus(search.query(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.content(), pageRequest.page(), pageRequest.size(), result.totalElements()
        ));
    }

    @Operation(summary = "리뷰 검색", description = "리뷰 탭 — 리뷰 내용 기반 검색. 이미지가 있는 리뷰의 대표 사진 1장 반환.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:reviews")
    @GetMapping("/v1/reviews")
    public ResponseEntity<ApiResponse<List<SearchReviewListItemResponse>>> searchReviews(
        @Valid @ModelAttribute SearchKeywordRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var result = searchQueryService.searchReviews(search.query(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.content(), pageRequest.page(), pageRequest.size(), result.totalElements()
        ));
    }

    @Operation(summary = "가게 검색", description = "가게 탭 — 가게명 기반 검색. 로그인 사용자 전용이며 북마크 여부가 포함됩니다.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:shops")
    @GetMapping("/v1/shops")
    public ResponseEntity<ApiResponse<List<SearchShopListItemResponse>>> searchShopsPaged(
        @Valid @ModelAttribute SearchKeywordRequest search,
        @Valid @ModelAttribute PageRequest pageRequest,
        @CurrentUser CustomUserDetails userDetails
    ) {
        var result = searchQueryService.searchShopsPaged(search.query(), userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.content(), pageRequest.page(), pageRequest.size(), result.totalElements()
        ));
    }

    @Operation(summary = "가게 검색 (비로그인)", description = "가게 탭 — 가게명 기반 검색. 인증 없이 접근 가능하며 북마크 여부는 항상 false로 응답합니다.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:shops:public")
    @GetMapping("/v1/shops/public")
    public ResponseEntity<ApiResponse<List<SearchShopListItemResponse>>> searchShopsPublic(
        @Valid @ModelAttribute SearchKeywordRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var result = searchQueryService.searchShopsPublic(search.query(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.content(), pageRequest.page(), pageRequest.size(), result.totalElements()
        ));
    }
}
