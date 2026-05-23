package com.tastyhouse.core.domain.point.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.domain.point.domain.model.QMemberPoint.memberPoint;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointJpaRepository memberPointJpaRepository;

    @Override
    public Optional<MemberPoint> findByMemberId(Long memberId) {
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
