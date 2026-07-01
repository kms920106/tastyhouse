package com.tastyhouse.core.domain.search.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;

public interface RecommendedKeywordJpaRepository extends JpaRepository<RecommendedKeyword, Long> {

    List<RecommendedKeyword> findByVisibleTrueOrderBySortOrderAsc();
}
