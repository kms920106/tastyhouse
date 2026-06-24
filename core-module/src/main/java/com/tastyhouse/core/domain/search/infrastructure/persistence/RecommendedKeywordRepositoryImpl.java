package com.tastyhouse.core.domain.search.infrastructure.persistence;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;
import com.tastyhouse.core.domain.search.domain.repository.RecommendedKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecommendedKeywordRepositoryImpl implements RecommendedKeywordRepository {

    private final RecommendedKeywordJpaRepository jpaRepository;

    @Override
    public List<RecommendedKeyword> findActiveOrderBySortOrder() {
        return jpaRepository.findByVisibleTrueOrderBySortOrderAsc();
    }
}
