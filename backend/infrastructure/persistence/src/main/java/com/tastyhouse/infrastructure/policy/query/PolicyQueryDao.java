package com.tastyhouse.infrastructure.policy.query;

import com.tastyhouse.application.policy.port.out.PolicyQueryPort;
import com.tastyhouse.application.policy.port.out.PolicyDocumentResult;
import com.tastyhouse.application.policy.port.out.PolicyListItemResult;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

@Repository
public class PolicyQueryDao implements PolicyQueryPort {
    private final JPAQueryFactory queryFactory;

    public PolicyQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<PolicyDocumentResult> findCurrentByType(PolicyType type) {
        PolicyDocumentResult result = queryFactory
            .select(policyDocumentDetailProjection())
            .from(policyDocumentJpaEntity)
            .where(
                policyDocumentJpaEntity.type.eq(type),
                policyDocumentJpaEntity.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version) {
        PolicyDocumentResult result = queryFactory
            .select(policyDocumentDetailProjection())
            .from(policyDocumentJpaEntity)
            .where(
                policyDocumentJpaEntity.type.eq(type),
                policyDocumentJpaEntity.version.eq(version)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public PageResult<PolicyListItemResult> findAllByType(PolicyType type, PageQuery pageQuery) {
        Long total = queryFactory
            .select(policyDocumentJpaEntity.id.count())
            .from(policyDocumentJpaEntity)
            .where(policyDocumentJpaEntity.type.eq(type))
            .fetchOne();

        List<PolicyListItemResult> policies = queryFactory
            .select(Projections.constructor(PolicyListItemResult.class,
                policyDocumentJpaEntity.id,
                policyDocumentJpaEntity.type,
                policyDocumentJpaEntity.version,
                policyDocumentJpaEntity.title,
                policyDocumentJpaEntity.current,
                policyDocumentJpaEntity.effectiveDate,
                policyDocumentJpaEntity.createdAt
            ))
            .from(policyDocumentJpaEntity)
            .where(policyDocumentJpaEntity.type.eq(type))
            .orderBy(policyDocumentJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(policies, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private ConstructorExpression<PolicyDocumentResult> policyDocumentDetailProjection() {
        return Projections.constructor(PolicyDocumentResult.class,
                policyDocumentJpaEntity.id,
            policyDocumentJpaEntity.type,
            policyDocumentJpaEntity.version,
            policyDocumentJpaEntity.title,
            policyDocumentJpaEntity.content,
            policyDocumentJpaEntity.current,
            policyDocumentJpaEntity.mandatory,
            policyDocumentJpaEntity.effectiveDate,
            policyDocumentJpaEntity.createdAt,
            policyDocumentJpaEntity.updatedAt
        );
    }
}
