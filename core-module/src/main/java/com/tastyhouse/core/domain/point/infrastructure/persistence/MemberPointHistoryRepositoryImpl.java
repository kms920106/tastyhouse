package com.tastyhouse.core.domain.point.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.point.domain.model.QMemberPointHistory.memberPointHistory;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryRepositoryImpl implements MemberPointHistoryRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;

    @Override
    public List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
        return queryFactory
            .selectFrom(memberPointHistory)
            .where(memberPointHistory.memberId.eq(memberId))
            .orderBy(memberPointHistory.createdAt.desc())
            .fetch();
    }

    @Override
    public MemberPointHistory save(MemberPointHistory history) {
        return memberPointHistoryJpaRepository.save(history);
    }
}
