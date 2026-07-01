package com.tastyhouse.core.domain.search.infrastructure.persistence;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;
import com.tastyhouse.core.domain.search.domain.repository.RecommendedKeywordRepository;

@Repository
@RequiredArgsConstructor
public class RecommendedKeywordRepositoryImpl implements RecommendedKeywordRepository {

    private final RecommendedKeywordJpaRepository jpaRepository;

    @Override
    public List<RecommendedKeyword> findActiveOrderBySortOrder() {
        return jpaRepository.findByVisibleTrueOrderBySortOrderAsc();
    }
}
