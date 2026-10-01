package com.tastyhouse.application.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.application.search.port.out.KeywordCount;
import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordPersistencePort;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogPersistencePort;

public class PopularKeywordRefreshService {

    private static final int AGGREGATION_WINDOW_DAYS = 7;

    private static final int LOG_RETENTION_DAYS = 30;

    private final SearchKeywordLogPersistencePort searchKeywordLogPersistencePort;
    private final KeywordCountPort keywordCountPort;
    private final PopularKeywordPersistencePort popularKeywordPersistencePort;

    public PopularKeywordRefreshService(
        SearchKeywordLogPersistencePort searchKeywordLogPersistencePort,
        KeywordCountPort keywordCountPort,
        PopularKeywordPersistencePort popularKeywordPersistencePort
    ) {
        this.searchKeywordLogPersistencePort = searchKeywordLogPersistencePort;
        this.keywordCountPort = keywordCountPort;
        this.popularKeywordPersistencePort = popularKeywordPersistencePort;
    }

    public void refresh() {
        LocalDateTime since = LocalDateTime.now().minusDays(AGGREGATION_WINDOW_DAYS);
        List<KeywordCount> rows = keywordCountPort.findTopKeywordsSince(since);

        Set<String> previousKeywords = popularKeywordPersistencePort.findActiveOrderByRank().stream()
            .map(PopularKeyword::getKeyword)
            .collect(Collectors.toSet());

        popularKeywordPersistencePort.deleteAll();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (KeywordCount row : rows) {
            String keyword = row.keyword();
            newRanks.add(PopularKeyword.of(keyword, rank++, !previousKeywords.contains(keyword)));
        }
        popularKeywordPersistencePort.saveAll(newRanks);
    }

    public void deleteOldSearchLogs() {
        searchKeywordLogPersistencePort.deleteOlderThan(LocalDateTime.now().minusDays(LOG_RETENTION_DAYS));
    }
}
