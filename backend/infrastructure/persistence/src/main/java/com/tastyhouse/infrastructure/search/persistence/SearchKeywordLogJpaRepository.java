package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordLogJpaRepository extends JpaRepository<SearchKeywordLogJpaEntity, Long> {
    @Modifying
    @Query("DELETE FROM SearchKeywordLogJpaEntity s WHERE s.searchedAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
}
