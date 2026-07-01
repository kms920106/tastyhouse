package com.tastyhouse.webapi.search;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.search.response.PopularKeywordResponse;
import com.tastyhouse.webapi.search.response.RecommendedKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapi.search.response.SearchShopListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 API")
public class SearchApiController {

    private final SearchService searchService;

    @Operation(summary = "인기 검색어 조회", description = "1~10위 인기 검색어 반환. 신규 진입 키워드는 isNew=true.")
    @GetMapping("/v1/popular-keywords")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> getPopularKeywords() {
        return ResponseEntity.ok(ApiResponse.success(searchService.getPopularKeywords()));
    }

    @Operation(summary = "추천 검색어 조회", description = "운영 관리 추천 검색어 태그 목록 반환.")
    @GetMapping("/v1/recommended-keywords")
    public ResponseEntity<ApiResponse<List<RecommendedKeywordResponse>>> getRecommendedKeywords() {
        return ResponseEntity.ok(ApiResponse.success(searchService.getRecommendedKeywords()));
    }

    @Operation(summary = "메뉴 검색", description = "메뉴 탭 — 메뉴명 기반 검색. 판매 중인 메뉴만 포함.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:menus")
    @GetMapping("/v1/menus")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> searchMenus(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        PageResponse<ProductSummaryResponse> result = searchService.searchMenus(keyword, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    @Operation(summary = "리뷰 검색", description = "리뷰 탭 — 리뷰 내용 기반 검색. 이미지가 있는 리뷰의 대표 사진 1장 반환.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:reviews")
    @GetMapping("/v1/reviews")
    public ResponseEntity<ApiResponse<List<SearchReviewListItemResponse>>> searchReviews(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        PageResponse<SearchReviewListItemResponse> result = searchService.searchReviews(keyword, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    @Operation(summary = "가게 검색", description = "가게 탭 — 가게명 기반 검색. 로그인 사용자 전용이며 북마크 여부가 포함됩니다.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:shops")
    @GetMapping("/v1/shops")
    public ResponseEntity<ApiResponse<List<SearchShopListItemResponse>>> searchShopsPaged(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest,
        @CurrentUser CustomUserDetails userDetails
    ) {
        String keyword = validateKeyword(query);
        PageResponse<SearchShopListItemResponse> result = searchService.searchShopsPaged(keyword, userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    @Operation(summary = "가게 검색 (비로그인)", description = "가게 탭 — 가게명 기반 검색. 인증 없이 접근 가능하며 북마크 여부는 항상 false로 응답합니다.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:shops:public")
    @GetMapping("/v1/shops/public")
    public ResponseEntity<ApiResponse<List<SearchShopListItemResponse>>> searchShopsPublic(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        PageResponse<SearchShopListItemResponse> result = searchService.searchShopsPublic(keyword, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }
}
