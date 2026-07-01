package com.tastyhouse.core.domain.policy.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.domain.policy.application.dto.result.QPolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.QPolicyListItemResult;
import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.policy.domain.model.QPolicyDocument.policyDocument;

@Repository
@RequiredArgsConstructor
public class PolicyDocumentRepositoryImpl implements PolicyDocumentRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<PolicyDocument> findById(PolicyDocumentId id) {
        PolicyDocument result = queryFactory
            .selectFrom(policyDocument)
            .where(policyDocument.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PolicyDocument> findCurrentEntityByType(PolicyType type) {
        PolicyDocument result = queryFactory
            .selectFrom(policyDocument)
            .where(
                policyDocument.type.eq(type),
                policyDocument.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PolicyDocumentResult> findCurrentByType(PolicyType type) {
        PolicyDocumentResult result = queryFactory
            .select(new QPolicyDocumentResult(
                policyDocument.id,
                policyDocument.type,
                policyDocument.version,
                policyDocument.title,
                policyDocument.content,
                policyDocument.current,
                policyDocument.mandatory,
                policyDocument.effectiveDate,
                policyDocument.createdAt,
                policyDocument.updatedAt
            ))
            .from(policyDocument)
            .where(
                policyDocument.type.eq(type),
                policyDocument.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PolicyDocumentResult> findByTypeAndVersion(PolicyType type, String version) {
        PolicyDocumentResult result = queryFactory
            .select(new QPolicyDocumentResult(
                policyDocument.id,
                policyDocument.type,
                policyDocument.version,
                policyDocument.title,
                policyDocument.content,
                policyDocument.current,
                policyDocument.mandatory,
                policyDocument.effectiveDate,
                policyDocument.createdAt,
                policyDocument.updatedAt
            ))
            .from(policyDocument)
            .where(
                policyDocument.type.eq(type),
                policyDocument.version.eq(version)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public PageResult<PolicyListItemResult> findAllByType(PolicyType type, PageQuery pageQuery) {
        JPAQuery<PolicyListItemResult> query = queryFactory
            .select(new QPolicyListItemResult(
                policyDocument.id,
                policyDocument.type,
                policyDocument.version,
                policyDocument.title,
                policyDocument.current,
                policyDocument.effectiveDate,
                policyDocument.createdAt
            ))
            .from(policyDocument)
            .where(policyDocument.type.eq(type))
            .orderBy(policyDocument.createdAt.desc());

        long total = query.fetch().size();

        List<PolicyListItemResult> policies = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(policies, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PolicyDocument save(PolicyDocument policyDocument) {
        if (policyDocument.getId() == null) {
            entityManager.persist(policyDocument);
            return policyDocument;
        }
        return entityManager.merge(policyDocument);
    }
}
