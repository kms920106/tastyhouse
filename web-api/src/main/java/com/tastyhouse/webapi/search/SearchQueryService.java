package com.tastyhouse.webapi.search;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemResult;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.review.query.ReviewQueryDao;
import com.tastyhouse.infrastructure.review.query.SearchReviewItemResult;
import com.tastyhouse.infrastructure.search.query.PopularKeywordResult;
import com.tastyhouse.infrastructure.search.query.RecommendedKeywordResult;
import com.tastyhouse.infrastructure.search.query.SearchQueryDao;
import com.tastyhouse.webapi.file.FileService;
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
 * <p>가게·메뉴·리뷰 검색은 다른 도메인(product/review/shop)의 read model에 위임한다. 리뷰 검색은 전환이
 * 끝나 infra query DAO({@link ReviewQueryDao})를 직접 주입하고, shop·product는 그 도메인의 infra query
 * DAO가 아직 신설되지 않았으므로(전환 그룹 3) 잠정적으로 기존 core 경로
 * ({@link ProductQueryService}·{@link ShopRepository})를 그대로 주입한다.
 * shop/product 전환 시 이 두 의존을 각 도메인의 infra query DAO 주입으로 교체해야 한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchQueryDao searchQueryDao;
    private final ProductQueryService productQueryService;
    private final ReviewQueryDao reviewQueryDao;
    private final ShopRepository shopRepository;
    private final FileService fileService;

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
        return shopRepository.searchByKeywordWithBookmark(keyword, MemberId.of(memberId), pageQuery)
            .map(this::toSearchShopListItemResponse);
    }

    public PageResult<SearchShopListItemResponse> searchShopsPublic(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopRepository.searchByKeywordWithBookmark(keyword, null, pageQuery)
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
            dto.imageFilePath() != null ? fileService.getUrlByPath(dto.imageFilePath()) : null,
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
        return SearchReviewListItemResponse.from(dto.id(), fileService.getUrlByPath(dto.imageFilePath()));
    }

    private SearchShopListItemResponse toSearchShopListItemResponse(ShopBookmarkedItemResult dto) {
        return SearchShopListItemResponse.from(
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
            dto.bookmarked()
        );
    }
}
