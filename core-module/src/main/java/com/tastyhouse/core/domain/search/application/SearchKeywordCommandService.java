package com.tastyhouse.core.domain.search.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;
import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;
import com.tastyhouse.core.domain.search.domain.repository.SearchKeywordLogRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchKeywordCommandService {

    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final PopularKeywordRepository popularKeywordRepository;

    public void aggregatePopularKeywords() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = searchKeywordLogRepository.findTop10KeywordsSince(since);

        Set<String> prevKeywords = popularKeywordRepository.findActiveOrderByRank()
            .stream().map(PopularKeyword::getKeyword).collect(Collectors.toSet());

        popularKeywordRepository.deleteAll();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String kw = (String) row[0];
            newRanks.add(PopularKeyword.of(kw, rank++, !prevKeywords.contains(kw)));
        }
        popularKeywordRepository.saveAll(newRanks);
    }

    public void deleteOldSearchLogs() {
        searchKeywordLogRepository.deleteOlderThan(LocalDateTime.now().minusDays(30));
    }
}
