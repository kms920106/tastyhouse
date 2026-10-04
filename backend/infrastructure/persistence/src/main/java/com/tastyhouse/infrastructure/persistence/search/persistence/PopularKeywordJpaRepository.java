package com.tastyhouse.infrastructure.persistence.search.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface PopularKeywordJpaRepository extends JpaRepository<PopularKeywordJpaEntity, Long> {

    List<PopularKeywordJpaEntity> findByVisibleTrueOrderByRankAsc();
}
