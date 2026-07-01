package com.tastyhouse.core.domain.search.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.search.application.dto.PopularKeywordResult;
import com.tastyhouse.core.domain.search.application.dto.RecommendedKeywordResult;
import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;
import com.tastyhouse.core.domain.search.domain.repository.RecommendedKeywordRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchKeywordQueryService {

    private final PopularKeywordRepository popularKeywordRepository;
    private final RecommendedKeywordRepository recommendedKeywordRepository;

    public List<PopularKeywordResult> findActivePopularKeywords() {
        return popularKeywordRepository.findActiveOrderByRank().stream()
            .map(PopularKeywordResult::from)
            .toList();
    }

    public List<RecommendedKeywordResult> findActiveRecommendedKeywords() {
        return recommendedKeywordRepository.findActiveOrderBySortOrder().stream()
            .map(RecommendedKeywordResult::from)
            .toList();
    }
}
