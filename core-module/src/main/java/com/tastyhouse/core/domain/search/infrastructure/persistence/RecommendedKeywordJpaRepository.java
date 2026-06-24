package com.tastyhouse.core.domain.search.infrastructure.persistence;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedKeywordJpaRepository extends JpaRepository<RecommendedKeyword, Long> {

    List<RecommendedKeyword> findByVisibleTrueOrderBySortOrderAsc();
}
