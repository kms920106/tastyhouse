package com.tastyhouse.infrastructure.persistence.search.persistence;

import java.time.LocalDateTime;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.model.SearchKeywordLog;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogPersistencePort;

import static com.tastyhouse.infrastructure.persistence.search.persistence.QSearchKeywordLogJpaEntity.searchKeywordLogJpaEntity;

@Repository
class SearchKeywordLogPersistenceAdapter implements SearchKeywordLogPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final SearchKeywordLogJpaRepository jpaRepository;

    public SearchKeywordLogPersistenceAdapter(JPAQueryFactory queryFactory, SearchKeywordLogJpaRepository jpaRepository) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SearchKeywordLog save(SearchKeywordLog log) {
        SearchKeywordLogJpaEntity saved = jpaRepository.save(SearchKeywordLogMapper.toEntity(log));
        return SearchKeywordLogMapper.toDomain(saved);
    }

    @Override
    public void deleteOlderThan(LocalDateTime before) {
        queryFactory.delete(searchKeywordLogJpaEntity)
            .where(searchKeywordLogJpaEntity.searchedAt.lt(before))
            .execute();
    }
}
