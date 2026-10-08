package com.tastyhouse.infrastructure.persistence.search.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SearchKeywordLogJpaRepository extends JpaRepository<SearchKeywordLogJpaEntity, Long> {
}
