package com.tastyhouse.webapi.search;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.SearchProductItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewQueryDao;
import com.tastyhouse.infrastructure.review.query.SearchReviewItemResult;
import com.tastyhouse.infrastructure.search.query.PopularKeywordResult;
import com.tastyhouse.infrastructure.search.query.RecommendedKeywordResult;
import com.tastyhouse.infrastructure.search.query.SearchQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopBookmarkedItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.webapi.product.ProductQueryService;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.search.response.SearchPopularKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchRecommendedKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapi.search.response.SearchShopListItemResponse;

/**
 * 검색 조회 서비스.
 *
 * <p>조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. 인기·추천 검색어는 infra read
 * 어댑터({@link SearchQueryDao})를 주입해 조회하고, Response 조립은 private 매퍼가 담당한다.
 *
 * <p>가게·메뉴·리뷰 검색은 다른 도메인(product/review/shop)의 read model에 위임한다. 리뷰·가게는 각
 * 도메인의 infra query DAO({@link ReviewQueryDao}·{@link ShopSearchQueryDao})를 직접 주입하고, 메뉴 검색은
 * 같은 모듈의 {@link ProductQueryService}(내부적으로 product infra query DAO를 소비)에 위임한다 —
 * 상품 검색 결과 조립은 product 도메인 소관이므로 그 QueryService를 재사용한다.
 */
@Service
@Transactional(readOnly = true)
public class SearchQueryService {

    private final SearchQueryDao searchQueryDao;
    private final ProductQueryService productQueryService;
    private final ReviewQueryDao reviewQueryDao;
    private final ShopSearchQueryDao shopSearchQueryDao;

    public SearchQueryService(
        SearchQueryDao searchQueryDao,
        ProductQueryService productQueryService,
        ReviewQueryDao reviewQueryDao,
        ShopSearchQueryDao shopSearchQueryDao
    ) {
        this.searchQueryDao = searchQueryDao;
        this.productQueryService = productQueryService;
        this.reviewQueryDao = reviewQueryDao;
        this.shopSearchQueryDao = shopSearchQueryDao;
    }

    public List<SearchPopularKeywordResponse> getPopularKeywords() {
        return searchQueryDao.findVisiblePopularKeywords().stream()
            .map(this::toSearchPopularKeywordResponse)
            .toList();
    }

    public List<SearchRecommendedKeywordResponse> getRecommendedKeywords() {
        return searchQueryDao.findVisibleRecommendedKeywords().stream()
            .map(this::toSearchRecommendedKeywordResponse)
            .toList();
    }

    public PageResult<ProductSummaryResponse> searchMenus(String query, int page, int size) {
        String keyword = validateKeyword(query);
        return productQueryService.searchByKeyword(keyword, page, size)
            .map(this::toProductSummaryResponse);
    }

    public PageResult<SearchReviewListItemResponse> searchReviews(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewQueryDao.searchByKeyword(keyword, pageQuery)
            .map(this::toSearchReviewListItemResponse);
    }

    public PageResult<SearchShopListItemResponse> searchShopsPaged(String query, Long memberId, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopSearchQueryDao.searchByKeywordWithBookmark(keyword, MemberId.of(memberId), pageQuery)
            .map(this::toSearchShopListItemResponse);
    }

    public PageResult<SearchShopListItemResponse> searchShopsPublic(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopSearchQueryDao.searchByKeywordWithBookmark(keyword, null, pageQuery)
            .map(this::toSearchShopListItemResponse);
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
            dto.bookmarked()
        );
    }
}
