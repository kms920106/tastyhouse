package com.tastyhouse.domain.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.domain.search.port.KeywordCount;
import com.tastyhouse.domain.search.port.KeywordCountPort;
import com.tastyhouse.domain.search.repository.PopularKeywordRepository;
import com.tastyhouse.domain.search.repository.SearchKeywordLogRepository;

public class PopularKeywordRefreshService {
    private static final int AGGREGATION_WINDOW_DAYS = 7;

    private static final int LOG_RETENTION_DAYS = 30;

    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final KeywordCountPort keywordCountPort;
    private final PopularKeywordRepository popularKeywordRepository;

    public PopularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        this.searchKeywordLogRepository = searchKeywordLogRepository;
        this.keywordCountPort = keywordCountPort;
        this.popularKeywordRepository = popularKeywordRepository;
    }

    public void refresh() {
        LocalDateTime since = LocalDateTime.now().minusDays(AGGREGATION_WINDOW_DAYS);
        List<KeywordCount> rows = keywordCountPort.findTopKeywordsSince(since);

        Set<String> previousKeywords = popularKeywordRepository.findActiveOrderByRank().stream()
            .map(PopularKeyword::getKeyword)
            .collect(Collectors.toSet());

        popularKeywordRepository.deleteAll();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (KeywordCount row : rows) {
            String keyword = row.keyword();
            newRanks.add(PopularKeyword.of(keyword, rank++, !previousKeywords.contains(keyword)));
        }
        popularKeywordRepository.saveAll(newRanks);
    }

    public void deleteOldSearchLogs() {
        searchKeywordLogRepository.deleteOlderThan(LocalDateTime.now().minusDays(LOG_RETENTION_DAYS));
    }
}
