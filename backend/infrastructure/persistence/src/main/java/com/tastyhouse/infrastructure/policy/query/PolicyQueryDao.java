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

/**
 * 정책 문서 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code PolicyDocumentRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api)의
 * {@code PolicyQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 정책 조회는 노출 제한이
 * 없어(활성/비활성 모두 공개 조회 가능) admin/web 구분이 필요하지 않으므로 메서드가 하나씩만 있다.
 */
@Repository
public class PolicyQueryDao implements PolicyQueryPort {

    private final JPAQueryFactory queryFactory;

    public PolicyQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 유형별 현행 정책 상세 조회 — 같은 유형에서 {@code current=true}인 단건을 조회한다.
     */
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

    /**
     * 유형·버전으로 정책 상세 조회 — 과거 버전 열람에 사용하므로 현행 여부를 따지지 않는다.
     */
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

    /**
     * 유형별 정책 버전 이력 목록 조회 — 최신 생성 순으로 페이징한다.
     */
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

    /**
     * 상세 조회 두 메서드가 같은 필드 셋을 투영하므로 프로젝션 정의를 공유한다.
     */
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
