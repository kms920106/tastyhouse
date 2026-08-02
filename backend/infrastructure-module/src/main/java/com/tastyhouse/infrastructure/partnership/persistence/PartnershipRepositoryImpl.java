package com.tastyhouse.infrastructure.partnership.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

/**
 * 제휴 신청 write 어댑터.
 *
 * <p>도메인 모델 단건 로드와 저장만 담당한다. 표현 목적 조회는 같은 모듈의
 * {@code partnership/query/PartnershipQueryDao}로 분리되어 있다.
 */
@Repository
public class PartnershipRepositoryImpl implements PartnershipRepository {

    private final JPAQueryFactory queryFactory;
    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    public PartnershipRepositoryImpl(JPAQueryFactory queryFactory, PartnershipRequestJpaRepository partnershipRequestJpaRepository) {
        this.queryFactory = queryFactory;
        this.partnershipRequestJpaRepository = partnershipRequestJpaRepository;
    }

    @Override
    public Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId) {
        if (partnershipRequestId == null) {
            return Optional.empty();
        }
        PartnershipRequestJpaEntity entity = queryFactory
            .selectFrom(partnershipRequestJpaEntity)
            .where(partnershipRequestJpaEntity.id.eq(partnershipRequestId.value()), partnershipRequestJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(PartnershipRequestMapper::toDomain);
    }

    @Override
    public PartnershipRequest save(PartnershipRequest partnershipRequest) {
        if (partnershipRequest.getId() == null) {
            PartnershipRequestJpaEntity saved = partnershipRequestJpaRepository.save(PartnershipRequestMapper.toEntity(partnershipRequest));
            return PartnershipRequestMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        // soft delete 갱신이 동작하도록 deleted 필터가 없는 순수 PK 조회를 사용한다.
        PartnershipRequestJpaEntity entity = partnershipRequestJpaRepository.findById(partnershipRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 제휴 문의입니다: " + partnershipRequest.getId()));
        PartnershipRequestMapper.applyChanges(entity, partnershipRequest);
        return PartnershipRequestMapper.toDomain(entity);
    }
}
