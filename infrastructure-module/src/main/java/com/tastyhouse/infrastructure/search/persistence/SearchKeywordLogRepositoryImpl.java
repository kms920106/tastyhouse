package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.domain.model.SearchKeywordLog;
import com.tastyhouse.domain.search.domain.repository.SearchKeywordLogRepository;

@Repository
public class SearchKeywordLogRepositoryImpl implements SearchKeywordLogRepository {

    private final SearchKeywordLogJpaRepository jpaRepository;

    public SearchKeywordLogRepositoryImpl(SearchKeywordLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SearchKeywordLog save(SearchKeywordLog log) {
        SearchKeywordLogJpaEntity saved = jpaRepository.save(SearchKeywordLogMapper.toEntity(log));
        return SearchKeywordLogMapper.toDomain(saved);
    }

    @Override
    public void deleteOlderThan(LocalDateTime before) {
        jpaRepository.deleteOlderThan(before);
    }
}
