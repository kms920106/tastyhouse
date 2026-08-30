package com.tastyhouse.webapplication.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.tastyhouse.application.search.port.out.SearchQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.webapplication.product.response.ProductSummaryResponse;
import com.tastyhouse.webapplication.product.service.ProductQueryService;
import com.tastyhouse.webapplication.search.port.in.SearchQueryUseCase;
import com.tastyhouse.webapplication.search.response.SearchPopularKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchRecommendedKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapplication.search.response.SearchShopListItemResponse;

/**
 * 검색 조회 서비스.
 *
 * <p>조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. 인기·추천 검색어는 infra read
 * 어댑터({@link SearchQueryPort})를 주입해 조회하고, Response 조립은 private 매퍼가 담당한다.
 *
 * <p>가게·메뉴·리뷰 검색은 다른 도메인(product/review/shop)의 read model에 위임한다. 리뷰·가게는 각
 * 도메인의 infra query DAO({@link ReviewQueryPort}·{@link ShopSearchQueryPort})를 직접 주입하고, 메뉴 검색은
 * 같은 모듈의 {@link ProductQueryService}(내부적으로 product infra query DAO를 소비)에 위임한다 —
 * 상품 검색 결과 조립은 product 도메인 소관이므로 그 QueryService를 재사용한다.
 */
@Service
@Transactional(readOnly = true)
public class SearchQueryService implements SearchQueryUseCase {

    private final SearchQueryPort searchQueryPort;
    private final ProductQueryService productQueryService;
    private final ReviewQueryPort reviewQueryPort;
    private final ShopSearchQueryPort shopSearchQueryPort;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;

    public SearchQueryService(
        SearchQueryPort searchQueryPort,
        ProductQueryService productQueryService,
        ReviewQueryPort reviewQueryPort,
        ShopSearchQueryPort shopSearchQueryPort,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort
    ) {
        this.searchQueryPort = searchQueryPort;
        this.productQueryService = productQueryService;
        this.reviewQueryPort = reviewQueryPort;
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
    }

    @Override
    public List<SearchPopularKeywordResponse> getPopularKeywords() {
        return searchQueryPort.findVisiblePopularKeywords().stream()
            .map(this::toSearchPopularKeywordResponse)
            .toList();
    }

    @Override
    public List<SearchRecommendedKeywordResponse> getRecommendedKeywords() {
        return searchQueryPort.findVisibleRecommendedKeywords().stream()
            .map(this::toSearchRecommendedKeywordResponse)
            .toList();
    }

    @Override
    public PaginationResponse<ProductSummaryResponse> searchMenus(String query, int page, int size) {
        String keyword = validateKeyword(query);
        return PaginationResponse.from(productQueryService.searchByKeyword(keyword, page, size)
            .map(this::toProductSummaryResponse));
    }

    @Override
    public PaginationResponse<SearchReviewListItemResponse> searchReviews(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(reviewQueryPort.searchByKeyword(keyword, pageQuery)
            .map(this::toSearchReviewListItemResponse));
    }

    @Override
    public PaginationResponse<SearchShopListItemResponse> searchShopsPaged(String query, Long memberId, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        Long deliveryAdminDongId = memberDeliveryAddressQueryPort
            .findDefaultAdminDongId(MemberId.of(memberId))
            .orElse(null);
        return PaginationResponse.from(shopSearchQueryPort.searchByKeywordWithBookmark(keyword, memberId, deliveryAdminDongId, pageQuery)
            .map(this::toSearchShopListItemResponse));
    }

    /** 비로그인 검색 — 배송지를 알 수 없으므로 배달지역 필터를 걸지 않는다. */
    @Override
    public PaginationResponse<SearchShopListItemResponse> searchShopsPublic(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(shopSearchQueryPort.searchByKeywordWithBookmark(keyword, null, null, pageQuery)
            .map(this::toSearchShopListItemResponse));
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }

    private SearchPopularKeywordResponse toSearchPopularKeywordResponse(PopularKeywordResult dto) {
        return SearchPopularKeywordResponse.of(dto.rank(), dto.keyword(), dto.newKeyword());
    }

    private SearchRecommendedKeywordResponse toSearchRecommendedKeywordResponse(RecommendedKeywordResult dto) {
        return SearchRecommendedKeywordResponse.of(dto.keyword());
    }

    private ProductSummaryResponse toProductSummaryResponse(SearchProductItemResult dto) {
        return ProductSummaryResponse.from(
            dto.id(),
            dto.name(),
            dto.imageUrl(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.rating(),
            dto.reviewCount(),
            dto.representative(),
            dto.spiciness()
        );
    }

    private SearchReviewListItemResponse toSearchReviewListItemResponse(SearchReviewItemResult dto) {
        return SearchReviewListItemResponse.from(dto.id(), dto.imageUrl());
    }

    private SearchShopListItemResponse toSearchShopListItemResponse(ShopBookmarkedItemResult dto) {
        return SearchShopListItemResponse.from(
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl(),
            dto.bookmarked(),
            dto.minOrderAmount(),
            dto.minDeliveryTip(),
            dto.maxDeliveryTip()
        );
    }
}
