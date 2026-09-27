package com.tastyhouse.infrastructure.policy.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentState;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentStatePort;

import static com.tastyhouse.infrastructure.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

@Repository
public class PolicyDocumentStatePortImpl implements PolicyDocumentStatePort {
    private final JPAQueryFactory queryFactory;
    private final PolicyDocumentJpaRepository policyDocumentJpaRepository;

    public PolicyDocumentStatePortImpl(JPAQueryFactory queryFactory, PolicyDocumentJpaRepository policyDocumentJpaRepository) {
        this.queryFactory = queryFactory;
        this.policyDocumentJpaRepository = policyDocumentJpaRepository;
    }

    @Override
    public Optional<PolicyDocumentState> findById(Long id) {
        PolicyDocumentJpaEntity result = queryFactory
            .selectFrom(policyDocumentJpaEntity)
            .where(policyDocumentJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result).map(PolicyDocumentMapper::toState);
    }

    @Override
    public Optional<PolicyDocumentState> findCurrentEntityByType(String type) {
        PolicyDocumentJpaEntity result = queryFactory
            .selectFrom(policyDocumentJpaEntity)
            .where(
                policyDocumentJpaEntity.type.eq(type),
                policyDocumentJpaEntity.current.isTrue()
            )
            .fetchOne();

        return Optional.ofNullable(result).map(PolicyDocumentMapper::toState);
    }

    @Override
    public PolicyDocumentState save(PolicyDocumentState state) {
        if (state.id() == null) {
            PolicyDocumentJpaEntity saved = policyDocumentJpaRepository.save(PolicyDocumentMapper.toEntity(state));
            return PolicyDocumentMapper.toState(saved);
        }

        PolicyDocumentJpaEntity entity = policyDocumentJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정책 문서입니다: " + state.id()));
        PolicyDocumentMapper.applyChanges(entity, state);
        return PolicyDocumentMapper.toState(entity);
    }
}
