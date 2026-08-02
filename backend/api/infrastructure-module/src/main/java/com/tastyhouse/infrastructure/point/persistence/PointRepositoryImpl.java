package com.tastyhouse.infrastructure.point.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.repository.PointRepository;

import static com.tastyhouse.infrastructure.point.persistence.QPointJpaEntity.pointJpaEntity;

@Repository
public class PointRepositoryImpl implements PointRepository {

    private final JPAQueryFactory queryFactory;
    private final PointJpaRepository pointJpaRepository;

    public PointRepositoryImpl(JPAQueryFactory queryFactory, PointJpaRepository pointJpaRepository) {
        this.queryFactory = queryFactory;
        this.pointJpaRepository = pointJpaRepository;
    }

    @Override
    public Optional<Point> findByMemberId(MemberId memberId) {
        PointJpaEntity result = queryFactory
            .selectFrom(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result).map(PointMapper::toDomain);
    }

    @Override
    public Point save(Point point) {
        if (point.getId() == null) {
            PointJpaEntity saved = pointJpaRepository.save(PointMapper.toEntity(point));
            return PointMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        PointJpaEntity entity = pointJpaRepository.findById(point.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 포인트입니다: " + point.getId()));
        PointMapper.applyChanges(entity, point);
        return PointMapper.toDomain(entity);
    }
}
