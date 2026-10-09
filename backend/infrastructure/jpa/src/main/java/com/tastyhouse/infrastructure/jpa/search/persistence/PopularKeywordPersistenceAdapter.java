package com.tastyhouse.infrastructure.jpa.search.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.application.search.port.out.write.PopularKeywordLoadPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordSavePort;

import static com.tastyhouse.infrastructure.jpa.search.persistence.QPopularKeywordJpaEntity.popularKeywordJpaEntity;

@Repository
class PopularKeywordPersistenceAdapter implements PopularKeywordLoadPort, PopularKeywordSavePort {

    private final JPAQueryFactory queryFactory;
    private final PopularKeywordJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public PopularKeywordPersistenceAdapter(
        JPAQueryFactory queryFactory,
        PopularKeywordJpaRepository jpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
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
