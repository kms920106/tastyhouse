package com.tastyhouse.infrastructure.partnership.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestState;
import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestStatePort;

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

@Repository
public class PartnershipRequestStatePortImpl implements PartnershipRequestStatePort {
    private final JPAQueryFactory queryFactory;
    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    public PartnershipRequestStatePortImpl(JPAQueryFactory queryFactory, PartnershipRequestJpaRepository partnershipRequestJpaRepository) {
        this.queryFactory = queryFactory;
        this.partnershipRequestJpaRepository = partnershipRequestJpaRepository;
    }

    @Override
    public Optional<PartnershipRequestState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        PartnershipRequestJpaEntity entity = queryFactory
            .selectFrom(partnershipRequestJpaEntity)
            .where(partnershipRequestJpaEntity.id.eq(id), partnershipRequestJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(PartnershipRequestMapper::toState);
    }

    @Override
    public PartnershipRequestState save(PartnershipRequestState state) {
        if (state.id() == null) {
            PartnershipRequestJpaEntity saved = partnershipRequestJpaRepository.save(PartnershipRequestMapper.toEntity(state));
            return PartnershipRequestMapper.toState(saved);
        }

        PartnershipRequestJpaEntity entity = partnershipRequestJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 제휴 문의입니다: " + state.id()));
        PartnershipRequestMapper.applyChanges(entity, state);
        return PartnershipRequestMapper.toState(entity);
    }
}
