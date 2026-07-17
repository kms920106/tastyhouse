package com.tastyhouse.core.domain.search.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;
import com.tastyhouse.core.domain.search.domain.repository.RecommendedKeywordRepository;
import com.tastyhouse.core.domain.search.application.dto.result.PopularKeywordResult;
import com.tastyhouse.core.domain.search.application.dto.result.RecommendedKeywordResult;

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
