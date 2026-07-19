package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordLogJpaRepository extends JpaRepository<SearchKeywordLogJpaEntity, Long> {

    @Query(value = """
            SELECT keyword, COUNT(*) as cnt
            FROM SEARCH_KEYWORD_LOG
            WHERE searched_at >= :since
            GROUP BY keyword
            ORDER BY cnt DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTop10KeywordsSince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM SearchKeywordLogJpaEntity s WHERE s.searchedAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
}
