package com.tastyhouse.webapi.search;

import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.search.application.SearchKeywordQueryService;
import com.tastyhouse.core.domain.search.application.SearchResultQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.search.response.PopularKeywordResponse;
import com.tastyhouse.webapi.search.response.RecommendedKeywordResponse;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.search.response.SearchShopListItemResponse;
import com.tastyhouse.webapi.search.response.SearchReviewListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchKeywordQueryService searchKeywordQueryService;
    private final SearchResultQueryService searchResultQueryService;
    private final FileService fileService;

    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchKeywordQueryService.findActivePopularKeywords().stream()
            .map(r -> PopularKeywordResponse.of(r.rank(), r.keyword(), r.isNew()))
            .toList();
    }

    public List<RecommendedKeywordResponse> getRecommendedKeywords() {
        return searchKeywordQueryService.findActiveRecommendedKeywords().stream()
            .map(r -> new RecommendedKeywordResponse(r.keyword()))
            .toList();
    }

    public PageResponse<ProductSummaryResponse> searchMenus(String keyword, int page, int size) {
        return PageResponse.from(searchResultQueryService.searchProducts(keyword, page, size))
            .map(dto -> ProductSummaryResponse.from(
                dto.id(),
                dto.name(),
                dto.imageFilePath() != null ? fileService.getUrlByPath(dto.imageFilePath()) : null,
                dto.originalPrice(),
                dto.discountPrice(),
                dto.discountRate(),
                dto.rating(),
                dto.reviewCount(),
                dto.isRepresentative(),
                dto.spiciness()
            ));
    }

    public PageResponse<SearchReviewListItemResponse> searchReviews(String keyword, int page, int size) {
        return PageResponse.from(searchResultQueryService.searchReviews(keyword, page, size))
            .map(dto -> SearchReviewListItemResponse.from(dto, fileService));
    }

    public PageResponse<SearchShopListItemResponse> searchShopsPaged(String keyword, Long memberId, int page, int size) {
        return PageResponse.from(searchResultQueryService.searchShopsWithBookmark(keyword, memberId, page, size))
            .map(dto -> SearchShopListItemResponse.from(
                dto.shopId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
                dto.bookmarked()
            ));
    }

    public PageResponse<SearchShopListItemResponse> searchShopsPublic(String keyword, int page, int size) {
        return PageResponse.from(searchResultQueryService.searchShopsWithBookmark(keyword, null, page, size))
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
