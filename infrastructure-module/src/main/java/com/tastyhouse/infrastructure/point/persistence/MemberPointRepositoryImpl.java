package com.tastyhouse.infrastructure.point.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;

import static com.tastyhouse.infrastructure.point.persistence.QMemberPointJpaEntity.memberPointJpaEntity;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointJpaRepository memberPointJpaRepository;

    @Override
    public Optional<MemberPoint> findByMemberId(MemberId memberId) {
        MemberPointJpaEntity result = queryFactory
            .selectFrom(memberPointJpaEntity)
            .where(memberPointJpaEntity.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result).map(MemberPointMapper::toDomain);
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        if (memberPoint.getId() == null) {
            MemberPointJpaEntity saved = memberPointJpaRepository.save(MemberPointMapper.toEntity(memberPoint));
            return MemberPointMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberPointJpaEntity entity = memberPointJpaRepository.findById(memberPoint.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 포인트입니다: " + memberPoint.getId()));
        MemberPointMapper.applyChanges(entity, memberPoint);
        return MemberPointMapper.toDomain(entity);
    }
}
