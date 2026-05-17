package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.SearchPlaceItemDto;
import com.tastyhouse.core.entity.product.dto.SearchProductItemDto;
import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.core.entity.search.PopularKeyword;
import com.tastyhouse.core.entity.search.RecommendedKeyword;
import com.tastyhouse.core.entity.search.SearchKeywordLog;
import com.tastyhouse.core.repository.place.PlaceRepository;
import com.tastyhouse.core.repository.product.ProductRepository;
import com.tastyhouse.core.repository.review.ReviewRepository;
import com.tastyhouse.core.repository.search.PopularKeywordJpaRepository;
import com.tastyhouse.core.repository.search.PopularKeywordRepository;
import com.tastyhouse.core.repository.search.RecommendedKeywordJpaRepository;
import com.tastyhouse.core.repository.search.SearchKeywordLogJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCoreService {

    private final SearchKeywordLogJpaRepository searchKeywordLogRepository;
    private final PopularKeywordJpaRepository popularKeywordJpaRepository;
    private final PopularKeywordRepository popularKeywordRepository;
    private final RecommendedKeywordJpaRepository recommendedKeywordRepository;
    private final PlaceRepository placeRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final EntityManager entityManager;

    @Transactional
    public void logSearch(String keyword) {
        searchKeywordLogRepository.save(SearchKeywordLog.of(keyword));
    }

    @Transactional(readOnly = true)
    public List<PopularKeyword> findActivePopularKeywords() {
        return popularKeywordJpaRepository.findByIsActiveTrueOrderByRankAsc();
    }

    @Transactional(readOnly = true)
    public List<RecommendedKeyword> findActiveRecommendedKeywords() {
        return recommendedKeywordRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public Page<BestPlaceItemDto> searchPlaces(String keyword, int page, int size) {
        return placeRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<SearchProductItemDto> searchProducts(String keyword, int page, int size) {
        return productRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<SearchReviewItemDto> searchReviews(String keyword, int page, int size) {
        return reviewRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<SearchPlaceItemDto> searchPlacesWithBookmark(String keyword, Long memberId, int page, int size) {
        return placeRepository.searchByKeywordWithBookmark(keyword, memberId, PageRequest.of(page, size));
    }

    @Transactional
    public void aggregatePopularKeywords() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = searchKeywordLogRepository.findTop10KeywordsSince(since);

        Set<String> prevKeywords = popularKeywordJpaRepository.findByIsActiveTrueOrderByRankAsc()
                .stream().map(PopularKeyword::getKeyword).collect(Collectors.toSet());

        popularKeywordRepository.deleteAllKeywords();
        entityManager.flush();
        entityManager.clear();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String kw = (String) row[0];
            newRanks.add(PopularKeyword.of(kw, rank++, !prevKeywords.contains(kw)));
        }
        popularKeywordJpaRepository.saveAll(newRanks);
    }

    @Transactional
    public void deleteOldSearchLogs() {
        searchKeywordLogRepository.deleteOlderThan(LocalDateTime.now().minusDays(30));
    }
}
