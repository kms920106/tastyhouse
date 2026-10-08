package com.tastyhouse.infrastructure.persistence.search.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PopularKeywordJpaRepository extends JpaRepository<PopularKeywordJpaEntity, Long> {
}
