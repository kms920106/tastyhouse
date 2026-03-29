package com.tastyhouse.core.repository.point;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.point.MemberPoint;
import com.tastyhouse.core.entity.point.MemberPointHistory;
import com.tastyhouse.core.entity.point.QMemberPoint;
import com.tastyhouse.core.entity.point.QMemberPointHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointJpaRepository memberPointJpaRepository;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;

    @Override
    public Optional<MemberPoint> findByMemberId(Long memberId) {
        QMemberPoint memberPoint = QMemberPoint.memberPoint;

        MemberPoint result = queryFactory
            .selectFrom(memberPoint)
            .where(memberPoint.memberId.eq(memberId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<MemberPointHistory> findPointHistoryByMemberIdOrderByCreatedAtDesc(Long memberId) {
        QMemberPointHistory memberPointHistory = QMemberPointHistory.memberPointHistory;

        return queryFactory
            .selectFrom(memberPointHistory)
            .where(memberPointHistory.memberId.eq(memberId))
            .orderBy(memberPointHistory.createdAt.desc())
            .fetch();
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        return memberPointJpaRepository.save(memberPoint);
    }

    @Override
    public MemberPointHistory saveHistory(MemberPointHistory memberPointHistory) {
        return memberPointHistoryJpaRepository.save(memberPointHistory);
    }
}
