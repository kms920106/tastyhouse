package com.tastyhouse.core.domain.search.infrastructure.persistence;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularKeywordJpaRepository extends JpaRepository<PopularKeyword, Long> {

    List<PopularKeyword> findByIsVisibleTrueOrderByRankAsc();
}
