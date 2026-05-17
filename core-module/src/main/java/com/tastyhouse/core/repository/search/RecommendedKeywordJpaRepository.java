package com.tastyhouse.core.repository.search;

import com.tastyhouse.core.entity.search.RecommendedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedKeywordJpaRepository extends JpaRepository<RecommendedKeyword, Long> {

    List<RecommendedKeyword> findByIsActiveTrueOrderBySortOrderAsc();
}
