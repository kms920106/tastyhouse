package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.search.port.out.write.SearchKeywordLogState;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogStatePort;

@Repository
public class SearchKeywordLogStatePortImpl implements SearchKeywordLogStatePort {
    private final SearchKeywordLogJpaRepository jpaRepository;

    public SearchKeywordLogStatePortImpl(SearchKeywordLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SearchKeywordLogState save(SearchKeywordLogState state) {
        SearchKeywordLogJpaEntity saved = jpaRepository.save(SearchKeywordLogMapper.toEntity(state));
        return SearchKeywordLogMapper.toState(saved);
    }

    @Override
    public void deleteOlderThan(LocalDateTime before) {
        jpaRepository.deleteOlderThan(before);
    }
}
