package com.tastyhouse.infrastructure.policy.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.domain.policy.domain.model.PolicyType;
import com.tastyhouse.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.domain.vo.PolicyDocumentId;

import static com.tastyhouse.infrastructure.policy.persistence.QPolicyDocumentJpaEntity.policyDocumentJpaEntity;

/**
 * 정책 문서 write 어댑터.
 *
 * <p>도메인 모델을 주고받는 command 경로만 담당한다. 표현 목적 조회(Result DTO 투영·페이징)는
 * 같은 모듈의 {@code policy/query/PolicyQueryDao}가 담당하므로 이 클래스에 두지 않는다.
 */
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
