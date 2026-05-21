package com.tastyhouse.webapi.search;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.service.SearchCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.search.response.PopularKeywordResponse;
import com.tastyhouse.webapi.search.response.RecommendedKeywordResponse;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.search.response.SearchPlaceListItem;
import com.tastyhouse.webapi.search.response.SearchReviewListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchCoreService searchCoreService;
    private final FileService fileService;

    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchCoreService.findActivePopularKeywords().stream()
                .map(pk -> PopularKeywordResponse.of(pk.getRank(), pk.getKeyword(), pk.getIsNew()))
                .toList();
    }

    public List<RecommendedKeywordResponse> getRecommendedKeywords() {
        return searchCoreService.findActiveRecommendedKeywords().stream()
                .map(rk -> new RecommendedKeywordResponse(rk.getKeyword()))
                .toList();
    }

    public PageResult<ProductSummaryResponse> searchMenus(String keyword, int page, int size) {
        return PageResult.from(searchCoreService.searchProducts(keyword, page, size))
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

    public PageResult<SearchReviewListItem> searchReviews(String keyword, int page, int size) {
        return PageResult.from(searchCoreService.searchReviews(keyword, page, size))
                .map(dto -> SearchReviewListItem.from(dto, fileService));
    }

    public PageResult<SearchPlaceListItem> searchPlacesPaged(String keyword, Long memberId, int page, int size) {
        return PageResult.from(searchCoreService.searchPlacesWithBookmark(keyword, memberId, page, size))
                .map(dto -> SearchPlaceListItem.from(
                    dto.placeId(),
                    dto.placeName(),
                    dto.stationName(),
                    dto.rating(),
                    dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
                    dto.isBookmarked()
                ));
    }
}
