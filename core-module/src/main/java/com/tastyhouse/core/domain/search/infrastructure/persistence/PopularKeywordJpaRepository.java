package com.tastyhouse.core.domain.search.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;

public interface PopularKeywordJpaRepository extends JpaRepository<PopularKeyword, Long> {

    List<PopularKeyword> findByVisibleTrueOrderByRankAsc();
}
