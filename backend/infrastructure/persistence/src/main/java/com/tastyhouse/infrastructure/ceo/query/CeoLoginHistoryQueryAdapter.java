package com.tastyhouse.infrastructure.ceo.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryResult;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistorySearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoLoginHistoryJpaEntity.ceoLoginHistoryJpaEntity;

@Repository
public class CeoLoginHistoryQueryAdapter implements CeoLoginHistoryQueryPort {

    private final JPAQueryFactory queryFactory;

    public CeoLoginHistoryQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<CeoLoginHistoryResult> findLoginHistoryPage(
        CeoLoginHistorySearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            ceoLoginHistoryJpaEntity.ceoId.eq(condition.ceoId()),
            resultEq(condition.result()),
            createdAtBetween(condition),
        };

        Long total = queryFactory
            .select(ceoLoginHistoryJpaEntity.count())
            .from(ceoLoginHistoryJpaEntity)
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<CeoLoginHistoryResult> content = queryFactory
            .select(Projections.constructor(CeoLoginHistoryResult.class,
                ceoLoginHistoryJpaEntity.id,
                ceoLoginHistoryJpaEntity.result,
                Expressions.nullExpression(String.class),
                ceoLoginHistoryJpaEntity.failureReason,
                Expressions.nullExpression(String.class),
                ceoLoginHistoryJpaEntity.ipAddress,
                ceoLoginHistoryJpaEntity.userAgent,
                ceoLoginHistoryJpaEntity.createdAt
            ))
            .from(ceoLoginHistoryJpaEntity)
            .where(predicates)
            .orderBy(ceoLoginHistoryJpaEntity.createdAt.desc(), ceoLoginHistoryJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression resultEq(String result) {
        return result != null ? ceoLoginHistoryJpaEntity.result.eq(result) : null;
    }

    private BooleanExpression createdAtBetween(CeoLoginHistorySearchCondition condition) {
        LocalDateTime from = condition.startDate().atStartOfDay();
        LocalDateTime until = condition.endDate().plusDays(1).atStartOfDay();
        return ceoLoginHistoryJpaEntity.createdAt.goe(from)
            .and(ceoLoginHistoryJpaEntity.createdAt.lt(until));
    }
}
