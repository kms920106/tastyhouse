package com.tastyhouse.core.domain.search.infrastructure.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;
import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;

import static com.tastyhouse.core.domain.search.domain.model.QPopularKeyword.popularKeyword;

@Repository
@RequiredArgsConstructor
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository {

    private final JPAQueryFactory queryFactory;
    private final PopularKeywordJpaRepository jpaRepository;

    @Override
    public List<PopularKeyword> findActiveOrderByRank() {
        return jpaRepository.findByVisibleTrueOrderByRankAsc();
    }

    @Override
    public List<PopularKeyword> saveAll(List<PopularKeyword> keywords) {
        return jpaRepository.saveAll(keywords);
    }

    @Override
    public void deleteAll() {
        queryFactory.delete(popularKeyword).execute();
    }
}
