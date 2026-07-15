package com.tastyhouse.core.domain.point.infrastructure.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;

import static com.tastyhouse.core.domain.point.domain.model.QMemberPoint.memberPoint;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointJpaRepository memberPointJpaRepository;

    @Override
    public Optional<MemberPoint> findByMemberId(MemberId memberId) {
        MemberPoint result = queryFactory
            .selectFrom(memberPoint)
            .where(memberPoint.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        return memberPointJpaRepository.save(memberPoint);
    }
}
