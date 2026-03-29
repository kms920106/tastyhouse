package com.tastyhouse.core.repository.policy;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.policy.PolicyDocument;
import com.tastyhouse.core.entity.policy.PolicyType;
import com.tastyhouse.core.entity.policy.QPolicyDocument;
import com.tastyhouse.core.entity.policy.dto.PolicyDocumentDto;
import com.tastyhouse.core.entity.policy.dto.PolicyListItemDto;
import com.tastyhouse.core.entity.policy.dto.QPolicyDocumentDto;
import com.tastyhouse.core.entity.policy.dto.QPolicyListItemDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PolicyDocumentRepositoryImpl implements PolicyDocumentRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<PolicyDocumentDto> findCurrentByType(PolicyType type) {
        QPolicyDocument policyDocument = QPolicyDocument.policyDocument;

        PolicyDocumentDto result = queryFactory
            .select(new QPolicyDocumentDto(
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
    public Optional<PolicyDocumentDto> findByTypeAndVersion(PolicyType type, String version) {
        QPolicyDocument policyDocument = QPolicyDocument.policyDocument;

        PolicyDocumentDto result = queryFactory
            .select(new QPolicyDocumentDto(
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
    public Optional<PolicyDocument> findById(Long id) {
        QPolicyDocument policyDocument = QPolicyDocument.policyDocument;

        PolicyDocument result = queryFactory
            .selectFrom(policyDocument)
            .where(policyDocument.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PolicyDocument> findCurrentEntityByType(PolicyType type) {
        QPolicyDocument policyDocument = QPolicyDocument.policyDocument;

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
    public PolicyDocument save(PolicyDocument policyDocument) {
        if (policyDocument.getId() == null) {
            entityManager.persist(policyDocument);
            return policyDocument;
        }
        return entityManager.merge(policyDocument);
    }

    @Override
    public Page<PolicyListItemDto> findAllByType(PolicyType type, Pageable pageable) {
        QPolicyDocument policyDocument = QPolicyDocument.policyDocument;

        JPAQuery<PolicyListItemDto> query = queryFactory
            .select(new QPolicyListItemDto(
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

        List<PolicyListItemDto> policies = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(policies, pageable, total);
    }
}
