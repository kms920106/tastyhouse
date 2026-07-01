package com.tastyhouse.core.domain.search.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.search.domain.model.SearchKeywordLog;
import com.tastyhouse.core.domain.search.domain.repository.SearchKeywordLogRepository;

@Repository
@RequiredArgsConstructor
public class SearchKeywordLogRepositoryImpl implements SearchKeywordLogRepository {

    private final SearchKeywordLogJpaRepository jpaRepository;

    @Override
    public SearchKeywordLog save(SearchKeywordLog log) {
        return jpaRepository.save(log);
    }

    @Override
    public List<Object[]> findTop10KeywordsSince(LocalDateTime since) {
        return jpaRepository.findTop10KeywordsSince(since);
    }

    @Override
    public void deleteOlderThan(LocalDateTime before) {
        jpaRepository.deleteOlderThan(before);
    }
}
