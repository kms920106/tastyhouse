package com.tastyhouse.infrastructure.search.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedKeywordJpaRepository extends JpaRepository<RecommendedKeywordJpaEntity, Long> {

    List<RecommendedKeywordJpaEntity> findByVisibleTrueOrderBySortOrderAsc();
}
