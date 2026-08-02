package com.tastyhouse.batch.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SearchKeywordScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchKeywordScheduler.class);

    private final SearchKeywordSchedulerService searchKeywordSchedulerService;

    public SearchKeywordScheduler(SearchKeywordSchedulerService searchKeywordSchedulerService) {
        this.searchKeywordSchedulerService = searchKeywordSchedulerService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void aggregatePopularKeywords() {
        log.info("=== 인기 검색어 집계 스케줄러 시작 ===");
        try {
            searchKeywordSchedulerService.aggregatePopularKeywords();
            log.info("=== 인기 검색어 집계 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("인기 검색어 집계 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void cleanUpOldSearchLogs() {
        log.info("=== 검색 로그 정리 스케줄러 시작 ===");
        try {
            searchKeywordSchedulerService.deleteOldSearchLogs();
            log.info("=== 검색 로그 정리 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("검색 로그 정리 중 오류 발생", e);
        }
    }
}
