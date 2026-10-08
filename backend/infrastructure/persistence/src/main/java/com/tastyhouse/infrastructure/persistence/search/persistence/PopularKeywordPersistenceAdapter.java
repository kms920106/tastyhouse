package com.tastyhouse.infrastructure.persistence.search.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.application.search.port.out.write.PopularKeywordPersistencePort;

import static com.tastyhouse.infrastructure.persistence.search.persistence.QPopularKeywordJpaEntity.popularKeywordJpaEntity;

@Repository
class PopularKeywordPersistenceAdapter implements PopularKeywordPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final PopularKeywordJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PopularKeywordPersistenceAdapter(JPAQueryFactory queryFactory, PopularKeywordJpaRepository jpaRepository) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<PopularKeyword> findActiveOrderByRank() {
        return queryFactory.selectFrom(popularKeywordJpaEntity)
            .where(popularKeywordJpaEntity.visible.isTrue())
            .orderBy(popularKeywordJpaEntity.rank.asc())
            .fetch()
            .stream()
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
