package com.tastyhouse.core.domain.point.infrastructure.persistence;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.point.domain.model.QMemberPointHistory.memberPointHistory;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryRepositoryImpl implements MemberPointHistoryRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;

    @Override
    public List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(MemberId memberId) {
        return queryFactory
            .selectFrom(memberPointHistory)
            .where(memberPointHistory.memberId.eq(memberId))
            .orderBy(memberPointHistory.createdAt.desc())
            .fetch();
    }

    @Override
    public PageResult<MemberPointHistory> findPointHistory(PointSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(memberPointHistory.id.count())
            .from(memberPointHistory)
            .where(
                memberPointHistory.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .fetchOne();

        List<MemberPointHistory> content = queryFactory
            .selectFrom(memberPointHistory)
            .where(
                memberPointHistory.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .orderBy(memberPointHistory.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public MemberPointHistory save(MemberPointHistory history) {
        return memberPointHistoryJpaRepository.save(history);
    }

    private BooleanExpression pointTypeEq(PointType pointType) {
        return pointType != null ? memberPointHistory.pointType.eq(pointType) : null;
    }
}
