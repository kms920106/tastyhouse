package com.tastyhouse.infrastructure.point.persistence;

import java.util.List;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.point.persistence.QMemberPointHistoryJpaEntity.memberPointHistoryJpaEntity;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryRepositoryImpl implements MemberPointHistoryRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;

    @Override
    public List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(MemberId memberId) {
        return queryFactory
            .selectFrom(memberPointHistoryJpaEntity)
            .where(memberPointHistoryJpaEntity.memberId.eq(memberId))
            .orderBy(memberPointHistoryJpaEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(MemberPointHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<MemberPointHistory> findPointHistory(PointSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(memberPointHistoryJpaEntity.id.count())
            .from(memberPointHistoryJpaEntity)
            .where(
                memberPointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .fetchOne();

        List<MemberPointHistory> content = queryFactory
            .selectFrom(memberPointHistoryJpaEntity)
            .where(
                memberPointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .orderBy(memberPointHistoryJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(MemberPointHistoryMapper::toDomain)
            .collect(Collectors.toList());

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public MemberPointHistory save(MemberPointHistory history) {
        MemberPointHistoryJpaEntity saved = memberPointHistoryJpaRepository.save(MemberPointHistoryMapper.toEntity(history));
        return MemberPointHistoryMapper.toDomain(saved);
    }

    private BooleanExpression pointTypeEq(PointType pointType) {
        return pointType != null ? memberPointHistoryJpaEntity.pointType.eq(pointType) : null;
    }
}
