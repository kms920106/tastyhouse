package com.tastyhouse.webapi.scheduler;

import com.tastyhouse.core.service.SearchCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchKeywordScheduler {

    private final SearchCoreService searchCoreService;

    @Scheduled(cron = "0 0 3 * * *")
    public void aggregatePopularKeywords() {
        log.info("=== 인기 검색어 집계 스케줄러 시작 ===");
        try {
            searchCoreService.aggregatePopularKeywords();
            log.info("=== 인기 검색어 집계 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("인기 검색어 집계 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void cleanUpOldSearchLogs() {
        log.info("=== 검색 로그 정리 스케줄러 시작 ===");
        try {
            searchCoreService.deleteOldSearchLogs();
            log.info("=== 검색 로그 정리 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("검색 로그 정리 중 오류 발생", e);
        }
    }
}
