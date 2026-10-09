package com.tastyhouse.infrastructure.jpa.policy.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentLoadPort;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentSavePort;

import static com.tastyhouse.infrastructure.jpa.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

@Repository
class PolicyDocumentPersistenceAdapter implements PolicyDocumentLoadPort, PolicyDocumentSavePort {

    private final JPAQueryFactory queryFactory;
    private final PolicyDocumentJpaRepository policyDocumentJpaRepository;

    public PolicyDocumentPersistenceAdapter(JPAQueryFactory queryFactory, PolicyDocumentJpaRepository policyDocumentJpaRepository) {
        this.queryFactory = queryFactory;
        this.policyDocumentJpaRepository = policyDocumentJpaRepository;
    }

    @Override
    public Optional<PolicyDocument> findById(PolicyDocumentId id) {
        return policyDocumentJpaRepository.findById(id.value()).map(PolicyDocumentMapper::toDomain);
    }

    @Override
    public Optional<PolicyDocument> findCurrentEntityByType(PolicyType type) {
        PolicyDocumentJpaEntity entity = queryFactory
            .selectFrom(policyDocumentJpaEntity)
            .where(
                policyDocumentJpaEntity.type.eq(type == null ? null : type.name()),
                policyDocumentJpaEntity.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(entity).map(PolicyDocumentMapper::toDomain);
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
