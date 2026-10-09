package com.tastyhouse.application.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.application.search.port.out.KeywordCount;
import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordLoadPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordSavePort;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogSavePort;

@Service
public class PopularKeywordRefreshService {

    private static final int AGGREGATION_WINDOW_DAYS = 7;

    private static final int LOG_RETENTION_DAYS = 30;

    private final SearchKeywordLogSavePort searchKeywordLogSavePort;
    private final KeywordCountPort keywordCountPort;
    private final PopularKeywordLoadPort popularKeywordLoadPort;
    private final PopularKeywordSavePort popularKeywordSavePort;

    public PopularKeywordRefreshService(
        SearchKeywordLogSavePort searchKeywordLogSavePort,
        KeywordCountPort keywordCountPort,
        PopularKeywordLoadPort popularKeywordLoadPort,
        PopularKeywordSavePort popularKeywordSavePort
    ) {
        this.searchKeywordLogSavePort = searchKeywordLogSavePort;
        this.keywordCountPort = keywordCountPort;
        this.popularKeywordLoadPort = popularKeywordLoadPort;
        this.popularKeywordSavePort = popularKeywordSavePort;
    }

    public void refresh() {
        LocalDateTime since = LocalDateTime.now().minusDays(AGGREGATION_WINDOW_DAYS);
        List<KeywordCount> rows = keywordCountPort.findTopKeywordsSince(since);

        Set<String> previousKeywords = popularKeywordLoadPort.findVisibleOrderByRank().stream()
            .map(PopularKeyword::getKeyword)
            .collect(Collectors.toSet());

        popularKeywordSavePort.deleteAll();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (KeywordCount row : rows) {
            String keyword = row.keyword();
            newRanks.add(PopularKeyword.of(keyword, rank++, !previousKeywords.contains(keyword)));
        }
        popularKeywordSavePort.saveAll(newRanks);
    }

    public void deleteOldSearchLogs() {
        searchKeywordLogSavePort.deleteOlderThan(LocalDateTime.now().minusDays(LOG_RETENTION_DAYS));
    }
}
