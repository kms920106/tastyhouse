package com.tastyhouse.infrastructure.search.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.domain.model.PopularKeyword;
import com.tastyhouse.domain.search.domain.repository.PopularKeywordRepository;

import static com.tastyhouse.infrastructure.search.persistence.QPopularKeywordJpaEntity.popularKeywordJpaEntity;

@Repository
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository {

    private final JPAQueryFactory queryFactory;
    private final PopularKeywordJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PopularKeywordRepositoryImpl(JPAQueryFactory queryFactory, PopularKeywordJpaRepository jpaRepository) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<PopularKeyword> findActiveOrderByRank() {
        return jpaRepository.findByVisibleTrueOrderByRankAsc().stream()
            .map(PopularKeywordMapper::toDomain)
            .toList();
    }

    @Override
    public List<PopularKeyword> saveAll(List<PopularKeyword> keywords) {
        List<PopularKeywordJpaEntity> entities = keywords.stream()
            .map(PopularKeywordMapper::toEntity)
            .toList();
        return jpaRepository.saveAll(entities).stream()
            .map(PopularKeywordMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAll() {
        queryFactory.delete(popularKeywordJpaEntity).execute();
        entityManager.flush();
        entityManager.clear();
    }
}
