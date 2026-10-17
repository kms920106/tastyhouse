package com.tastyhouse.infrastructure.jpa.point.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.application.point.port.out.write.PointLoadPort;
import com.tastyhouse.application.point.port.out.write.PointSavePort;

import static com.tastyhouse.infrastructure.jpa.point.persistence.QPointJpaEntity.pointJpaEntity;

@Repository
class PointPersistenceAdapter implements PointLoadPort, PointSavePort {

    private static final String INSERT_IF_ABSENT_SQL = """
        INSERT INTO POINT (member_id, available_points, expired_this_month, created_at, updated_at)
        VALUES (:memberId, 0, 0, :now, :now)
        ON DUPLICATE KEY UPDATE member_id = member_id
        """;

    private final JPAQueryFactory queryFactory;
    private final PointJpaRepository pointJpaRepository;
    private final EntityManager entityManager;

    public PointPersistenceAdapter(
        JPAQueryFactory queryFactory,
        PointJpaRepository pointJpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.pointJpaRepository = pointJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Point> findByMemberIdForUpdate(MemberId memberId) {
        PointJpaEntity entity = queryFactory
            .selectFrom(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId.value()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
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

    @Override
    public void saveIfAbsent(MemberId memberId) {
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery(INSERT_IF_ABSENT_SQL)
            .setParameter("memberId", memberId.value())
            .setParameter("now", now)
            .executeUpdate();
    }
}
