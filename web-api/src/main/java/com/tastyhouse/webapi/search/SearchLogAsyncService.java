package com.tastyhouse.webapi.search;

import com.tastyhouse.core.service.SearchCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogAsyncService {

    private final SearchCoreService searchCoreService;

    @Async
    public void log(String keyword) {
        try {
            searchCoreService.logSearch(keyword);
        } catch (Exception e) {
            log.warn("검색 로그 저장 실패: {}", keyword, e);
        }
    }
}
