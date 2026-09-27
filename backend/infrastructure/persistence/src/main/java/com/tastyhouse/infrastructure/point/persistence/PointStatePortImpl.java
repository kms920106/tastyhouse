package com.tastyhouse.infrastructure.point.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.point.port.out.write.PointState;
import com.tastyhouse.application.point.port.out.write.PointStatePort;

import static com.tastyhouse.infrastructure.point.persistence.QPointJpaEntity.pointJpaEntity;

@Repository
public class PointStatePortImpl implements PointStatePort {
    private final JPAQueryFactory queryFactory;
    private final PointJpaRepository pointJpaRepository;

    public PointStatePortImpl(JPAQueryFactory queryFactory, PointJpaRepository pointJpaRepository) {
        this.queryFactory = queryFactory;
        this.pointJpaRepository = pointJpaRepository;
    }

    @Override
    public Optional<PointState> findByMemberId(Long memberId) {
        PointJpaEntity result = queryFactory
            .selectFrom(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result).map(PointMapper::toState);
    }

    @Override
    public PointState save(PointState state) {
        if (state.id() == null) {
            PointJpaEntity saved = pointJpaRepository.save(PointMapper.toEntity(state));
            return PointMapper.toState(saved);
        }

        PointJpaEntity entity = pointJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 포인트입니다: " + state.id()));
        PointMapper.applyChanges(entity, state);
        return PointMapper.toState(entity);
    }
}
