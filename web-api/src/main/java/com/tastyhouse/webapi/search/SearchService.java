package com.tastyhouse.webapi.search;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.search.application.SearchKeywordQueryService;
import com.tastyhouse.core.domain.search.application.SearchResultQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.search.response.SearchPopularKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchRecommendedKeywordResponse;
import com.tastyhouse.webapi.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapi.search.response.SearchShopListItemResponse;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchKeywordQueryService searchKeywordQueryService;
    private final SearchResultQueryService searchResultQueryService;
    private final FileService fileService;

    public List<SearchPopularKeywordResponse> getPopularKeywords() {
        return searchKeywordQueryService.findActivePopularKeywords().stream()
            .map(r -> SearchPopularKeywordResponse.of(r.rank(), r.keyword(), r.newKeyword()))
            .toList();
    }

    public List<SearchRecommendedKeywordResponse> getRecommendedKeywords() {
        return searchKeywordQueryService.findActiveRecommendedKeywords().stream()
            .map(r -> SearchRecommendedKeywordResponse.of(r.keyword()))
            .toList();
    }

    public PageResult<ProductSummaryResponse> searchMenus(String keyword, int page, int size) {
        return searchResultQueryService.searchProducts(keyword, page, size)
            .map(dto -> ProductSummaryResponse.from(
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
            ));
    }

    public PageResult<SearchReviewListItemResponse> searchReviews(String keyword, int page, int size) {
        return searchResultQueryService.searchReviews(keyword, page, size)
            .map(dto -> SearchReviewListItemResponse.from(dto.id(), fileService.getUrlByPath(dto.imageFilePath())));
    }

    public PageResult<SearchShopListItemResponse> searchShopsPaged(String keyword, Long memberId, int page, int size) {
        return searchResultQueryService.searchShopsWithBookmark(keyword, MemberId.of(memberId), page, size)
            .map(dto -> SearchShopListItemResponse.from(
                dto.shopId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
                dto.bookmarked()
            ));
    }

    public PageResult<SearchShopListItemResponse> searchShopsPublic(String keyword, int page, int size) {
        return searchResultQueryService.searchShopsWithBookmark(keyword, null, page, size)
            .map(dto -> SearchShopListItemResponse.from(
                dto.shopId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
                dto.bookmarked()
            ));
    }
}
