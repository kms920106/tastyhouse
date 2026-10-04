package com.tastyhouse.infrastructure.persistence.search.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.model.SearchKeywordLog;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogPersistencePort;

@Repository
public class SearchKeywordLogPersistenceAdapter implements SearchKeywordLogPersistencePort {

    private final SearchKeywordLogJpaRepository jpaRepository;

    public SearchKeywordLogPersistenceAdapter(SearchKeywordLogJpaRepository jpaRepository) {
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
