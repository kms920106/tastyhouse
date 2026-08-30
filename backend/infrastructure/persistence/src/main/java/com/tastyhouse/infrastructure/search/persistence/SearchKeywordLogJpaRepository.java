package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 검색 키워드 로그 JPA 리포지토리.
 *
 * <p>키워드별 검색 수 집계는 타입 없는 {@code Object[]} 튜플을 돌려주던 네이티브 쿼리 대신
 * {@code search/query/SearchQueryDao}의 QueryDSL 투영이 담당한다.
 */
public interface SearchKeywordLogJpaRepository extends JpaRepository<SearchKeywordLogJpaEntity, Long> {

    @Modifying
    @Query("DELETE FROM SearchKeywordLogJpaEntity s WHERE s.searchedAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
}
