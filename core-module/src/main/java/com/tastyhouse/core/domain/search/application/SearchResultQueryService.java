package com.tastyhouse.core.domain.search.application;

import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemDto;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchResultQueryService {

    private final ProductQueryService productQueryService;
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;

    public Page<SearchProductItemResult> searchProducts(String keyword, int page, int size) {
        return productQueryService.searchByKeyword(keyword, page, size);
    }

    public Page<SearchReviewItemResult> searchReviews(String keyword, int page, int size) {
        return reviewRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    public Page<ShopBookmarkedItemDto> searchShopsWithBookmark(String keyword, Long memberId, int page, int size) {
        return shopRepository.searchByKeywordWithBookmark(keyword, memberId, PageRequest.of(page, size));
    }
}
