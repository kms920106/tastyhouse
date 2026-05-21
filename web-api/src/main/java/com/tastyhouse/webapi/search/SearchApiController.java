package com.tastyhouse.webapi.search;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.search.response.SearchPlaceListItem;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.search.response.PopularKeywordResponse;
import com.tastyhouse.webapi.search.response.RecommendedKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchReviewListItem;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 API")
public class SearchApiController {

    private final SearchService searchService;

    @Operation(summary = "인기 검색어 조회", description = "1~10위 인기 검색어 반환. 신규 진입 키워드는 isNew=true.")
    @GetMapping("/v1/popular-keywords")
    public ResponseEntity<CommonResponse<List<PopularKeywordResponse>>> getPopularKeywords() {
        return ResponseEntity.ok(CommonResponse.success(searchService.getPopularKeywords()));
    }

    @Operation(summary = "추천 검색어 조회", description = "운영 관리 추천 검색어 태그 목록 반환.")
    @GetMapping("/v1/recommended-keywords")
    public ResponseEntity<CommonResponse<List<RecommendedKeywordResponse>>> getRecommendedKeywords() {
        return ResponseEntity.ok(CommonResponse.success(searchService.getRecommendedKeywords()));
    }

    @Operation(summary = "메뉴 검색", description = "메뉴 탭 — 메뉴명 기반 검색. 판매 중인 메뉴만 포함.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:menus")
    @GetMapping("/v1/menus")
    public ResponseEntity<CommonResponse<List<ProductSummaryResponse>>> searchMenus(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        PageResult<ProductSummaryResponse> result = searchService.searchMenus(keyword, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    @Operation(summary = "리뷰 검색", description = "리뷰 탭 — 리뷰 내용 기반 검색. 이미지가 있는 리뷰의 대표 사진 1장 반환.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:reviews")
    @GetMapping("/v1/reviews")
    public ResponseEntity<CommonResponse<List<SearchReviewListItem>>> searchReviews(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        PageResult<SearchReviewListItem> result = searchService.searchReviews(keyword, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            result.getContent(), pageRequest.page(), pageRequest.size(), result.getTotalElements()
        ));
    }

    @Operation(summary = "플레이스 검색", description = "플레이스 탭 — 플레이스명 기반 검색. 로그인 시 북마크 여부 포함.")
    @RateLimit(limit = 30, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:search:places")
    @GetMapping("/v1/places")
    public ResponseEntity<CommonResponse<List<SearchPlaceListItem>>> searchPlacesPaged(
        @RequestParam String query,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        String keyword = validateKeyword(query);
        Long memberId = resolveCurrentMemberId();
        PageResult<SearchPlaceListItem> result = searchService.searchPlacesPaged(keyword, memberId, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
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

    private Long resolveCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
            && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getMemberId();
        }
        return null;
    }
}
