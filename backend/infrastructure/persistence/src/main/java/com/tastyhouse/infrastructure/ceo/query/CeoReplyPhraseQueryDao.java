package com.tastyhouse.infrastructure.ceo.query;

import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoReplyPhraseJpaEntity.ceoReplyPhraseJpaEntity;

@Repository
public class CeoReplyPhraseQueryDao implements CeoReplyPhraseQueryPort {
    private final JPAQueryFactory queryFactory;

    public CeoReplyPhraseQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CeoReplyPhraseResult> findReplyPhrases(Long ceoId) {
        return queryFactory
            .select(Projections.constructor(CeoReplyPhraseResult.class,
                ceoReplyPhraseJpaEntity.id,
                ceoReplyPhraseJpaEntity.name,
                ceoReplyPhraseJpaEntity.content,
                ceoReplyPhraseJpaEntity.sort,
                ceoReplyPhraseJpaEntity.createdAt
            ))
            .from(ceoReplyPhraseJpaEntity)
            .where(ceoReplyPhraseJpaEntity.ceoId.eq(ceoId))
            .orderBy(ceoReplyPhraseJpaEntity.sort.asc(), ceoReplyPhraseJpaEntity.id.asc())
            .fetch();
    }
}
