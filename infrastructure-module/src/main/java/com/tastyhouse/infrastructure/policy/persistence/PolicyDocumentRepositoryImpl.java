package com.tastyhouse.infrastructure.policy.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.PolicyListItemResult;
import com.tastyhouse.core.domain.policy.application.dto.result.QPolicyDocumentResult;
import com.tastyhouse.core.domain.policy.application.dto.result.QPolicyListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

@Repository
@RequiredArgsConstructor
public class PolicyDocumentRepositoryImpl implements PolicyDocumentRepository {

    private final JPAQueryFactory queryFactory;
    private final PolicyDocumentJpaRepository policyDocumentJpaRepository;

    @Override
    public Optional<PolicyDocument> findById(PolicyDocumentId id) {
        PolicyDocumentJpaEntity result = queryFactory
            .selectFrom(policyDocumentJpaEntity)
            .where(policyDocumentJpaEntity.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result).map(PolicyDocumentMapper::toDomain);
    }

    @Override
    public Optional<PolicyDocument> findCurrentEntityByType(PolicyType type) {
        PolicyDocumentJpaEntity result = queryFactory
            .selectFrom(policyDocumentJpaEntity)
            .where(
                policyDocumentJpaEntity.type.eq(type),
                policyDocumentJpaEntity.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(result).map(PolicyDocumentMapper::toDomain);
    }

    @Override
    public Optional<PolicyDocumentResult> findCurrentByType(PolicyType type) {
        PolicyDocumentResult result = queryFactory
            .select(new QPolicyDocumentResult(
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
            ))
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
            .select(new QPolicyDocumentResult(
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
            ))
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
        JPAQuery<PolicyListItemResult> query = queryFactory
            .select(new QPolicyListItemResult(
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
            .orderBy(policyDocumentJpaEntity.createdAt.desc());

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
            PolicyDocumentJpaEntity saved = policyDocumentJpaRepository.save(PolicyDocumentMapper.toEntity(policyDocument));
            return PolicyDocumentMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        PolicyDocumentJpaEntity entity = policyDocumentJpaRepository.findById(policyDocument.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정책 문서입니다: " + policyDocument.getId()));
        PolicyDocumentMapper.applyChanges(entity, policyDocument);
        return PolicyDocumentMapper.toDomain(entity);
    }
}
