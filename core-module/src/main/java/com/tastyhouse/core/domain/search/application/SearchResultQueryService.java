package com.tastyhouse.core.domain.search.application;

import com.tastyhouse.core.entity.place.dto.PlaceBookmarkedItemDto;
import com.tastyhouse.core.entity.product.dto.SearchProductItemDto;
import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.core.repository.place.PlaceRepository;
import com.tastyhouse.core.repository.product.ProductRepository;
import com.tastyhouse.core.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchResultQueryService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;

    public Page<SearchProductItemDto> searchProducts(String keyword, int page, int size) {
        return productRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    public Page<SearchReviewItemDto> searchReviews(String keyword, int page, int size) {
        return reviewRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    public Page<PlaceBookmarkedItemDto> searchPlacesWithBookmark(String keyword, Long memberId, int page, int size) {
        return placeRepository.searchByKeywordWithBookmark(keyword, memberId, PageRequest.of(page, size));
    }
}
