package com.tastyhouse.infrastructure.ceo.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoReplyPhraseJpaEntity.ceoReplyPhraseJpaEntity;

/**
 * 자주 쓰는 문구 read 어댑터(CQRS query 측).
 *
 * <p>페이징이 없다 — 점주당 5건 상한이라 한 번에 전부 내려주는 편이 단순하고, 페이지 파라미터를 두면
 * 프론트가 쓰지 않을 분기를 떠안게 된다.
 */
@Repository
public class CeoReplyPhraseQueryDao {

    private final JPAQueryFactory queryFactory;

    public CeoReplyPhraseQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 점주 본인의 자주 쓰는 문구 전체를 정렬 순서({@code sort ASC, id ASC})로 조회한다.
     *
     * <p>{@code id}를 2차 정렬 키로 두는 이유는 삭제 후 {@code sort}를 재정렬하지 않아 순번이 같은 행이
     * 생길 수 있기 때문이다 — 동률일 때 등록순으로 안정 정렬된다.
     */
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
