package com.tastyhouse.core.repository.search;

import com.tastyhouse.core.entity.search.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularKeywordJpaRepository extends JpaRepository<PopularKeyword, Long> {

    List<PopularKeyword> findByIsActiveTrueOrderByRankAsc();
}
