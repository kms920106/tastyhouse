package com.tastyhouse.infrastructure.partnership.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

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

        PartnershipRequestJpaEntity entity = partnershipRequestJpaRepository.findById(partnershipRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 제휴 문의입니다: " + partnershipRequest.getId()));
        PartnershipRequestMapper.applyChanges(entity, partnershipRequest);
        return PartnershipRequestMapper.toDomain(entity);
    }
}
