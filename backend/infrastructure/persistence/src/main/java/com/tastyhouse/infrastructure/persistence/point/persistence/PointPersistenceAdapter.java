package com.tastyhouse.infrastructure.persistence.point.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.application.point.port.out.write.PointLoadPort;
import com.tastyhouse.application.point.port.out.write.PointSavePort;

import static com.tastyhouse.infrastructure.persistence.point.persistence.QPointJpaEntity.pointJpaEntity;

@Repository
class PointPersistenceAdapter implements PointLoadPort, PointSavePort {

    private final JPAQueryFactory queryFactory;
    private final PointJpaRepository pointJpaRepository;

    public PointPersistenceAdapter(JPAQueryFactory queryFactory, PointJpaRepository pointJpaRepository) {
        this.queryFactory = queryFactory;
        this.pointJpaRepository = pointJpaRepository;
    }

    @Override
    public Optional<Point> findByMemberId(MemberId memberId) {
        PointJpaEntity entity = queryFactory
            .selectFrom(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(PointMapper::toDomain);
    }

    @Override
    public Point save(Point point) {
        if (point.getId() == null) {
            PointJpaEntity saved = pointJpaRepository.save(PointMapper.toEntity(point));
            return PointMapper.toDomain(saved);
        }

        PointJpaEntity entity = pointJpaRepository.findById(point.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 포인트입니다: " + point.getId()));
        PointMapper.applyChanges(entity, point);
        return PointMapper.toDomain(entity);
    }
}
