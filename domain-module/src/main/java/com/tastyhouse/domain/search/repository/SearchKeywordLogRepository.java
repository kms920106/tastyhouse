package com.tastyhouse.domain.search.repository;

import java.time.LocalDateTime;

import com.tastyhouse.domain.search.model.SearchKeywordLog;
import com.tastyhouse.domain.search.port.KeywordCountPort;

/**
 * 검색 키워드 로그 write 포트.
 *
 * <p>키워드별 검색 수 집계는 인프라 투영이 필요한 조회이므로 이 포트가 아니라
 * {@link KeywordCountPort}가 담당한다.
 */
public interface SearchKeywordLogRepository {

    SearchKeywordLog save(SearchKeywordLog log);

    void deleteOlderThan(LocalDateTime before);
}
