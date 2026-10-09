package com.tastyhouse.infrastructure.jpa.search.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SearchKeywordLogJpaRepository extends JpaRepository<SearchKeywordLogJpaEntity, Long> {
}
