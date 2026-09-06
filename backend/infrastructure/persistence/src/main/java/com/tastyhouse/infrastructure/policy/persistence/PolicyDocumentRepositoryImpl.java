package com.tastyhouse.infrastructure.policy.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

import static com.tastyhouse.infrastructure.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

@Repository
public class PolicyDocumentRepositoryImpl implements PolicyDocumentRepository {
    private final JPAQueryFactory queryFactory;
    private final PolicyDocumentJpaRepository policyDocumentJpaRepository;

    public PolicyDocumentRepositoryImpl(JPAQueryFactory queryFactory, PolicyDocumentJpaRepository policyDocumentJpaRepository) {
        this.queryFactory = queryFactory;
        this.policyDocumentJpaRepository = policyDocumentJpaRepository;
    }

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
    public PolicyDocument save(PolicyDocument policyDocument) {
        if (policyDocument.getId() == null) {
            PolicyDocumentJpaEntity saved = policyDocumentJpaRepository.save(PolicyDocumentMapper.toEntity(policyDocument));
            return PolicyDocumentMapper.toDomain(saved);
        }

        PolicyDocumentJpaEntity entity = policyDocumentJpaRepository.findById(policyDocument.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정책 문서입니다: " + policyDocument.getId()));
        PolicyDocumentMapper.applyChanges(entity, policyDocument);
        return PolicyDocumentMapper.toDomain(entity);
    }
}
