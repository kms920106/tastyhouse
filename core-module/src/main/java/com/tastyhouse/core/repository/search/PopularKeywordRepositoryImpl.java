package com.tastyhouse.core.repository.search;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.core.entity.search.QPopularKeyword.popularKeyword;

@Repository
@RequiredArgsConstructor
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllKeywords() {
        queryFactory.delete(popularKeyword).execute();
    }
}
