package com.tastyhouse.infrastructure.search.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.search.port.out.write.PopularKeywordState;
import com.tastyhouse.application.search.port.out.write.PopularKeywordStatePort;

import static com.tastyhouse.infrastructure.search.persistence.QPopularKeywordJpaEntity.popularKeywordJpaEntity;

@Repository
public class PopularKeywordStatePortImpl implements PopularKeywordStatePort {
    private final JPAQueryFactory queryFactory;
    private final PopularKeywordJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PopularKeywordStatePortImpl(JPAQueryFactory queryFactory, PopularKeywordJpaRepository jpaRepository) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<PopularKeywordState> findActiveOrderByRank() {
        return jpaRepository.findByVisibleTrueOrderByRankAsc().stream()
            .map(PopularKeywordMapper::toState)
            .toList();
    }

    @Override
    public List<PopularKeywordState> saveAll(List<PopularKeywordState> states) {
        List<PopularKeywordJpaEntity> entities = states.stream()
            .map(PopularKeywordMapper::toEntity)
            .toList();
        return jpaRepository.saveAll(entities).stream()
            .map(PopularKeywordMapper::toState)
            .toList();
    }

    @Override
    public void deleteAll() {
        queryFactory.delete(popularKeywordJpaEntity).execute();
        entityManager.flush();
        entityManager.clear();
    }
}
