package com.tastyhouse.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.search.domain.service.PopularKeywordRefreshService;

/**
 * 검색 키워드 배치 application 서비스.
 *
 * <p>인기 검색어 갱신은 액터 무관 도메인 규칙이라 도메인 서비스
 * {@link PopularKeywordRefreshService}(순수 POJO)가 소유한다. 이 서비스는 트랜잭션 경계만 제공해,
 * 인기 검색어 전체 삭제 후 재저장이 한 트랜잭션에서 원자적으로 수행되도록 보장한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SearchKeywordSchedulerService {

    private final PopularKeywordRefreshService popularKeywordRefreshService;

    public void aggregatePopularKeywords() {
        popularKeywordRefreshService.refresh();
    }

    public void deleteOldSearchLogs() {
        popularKeywordRefreshService.deleteOldSearchLogs();
    }
}
