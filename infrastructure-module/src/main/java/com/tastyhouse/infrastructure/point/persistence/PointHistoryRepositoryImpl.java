package com.tastyhouse.infrastructure.point.persistence;

import java.util.List;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.PointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.PointHistoryRepository;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.point.persistence.QPointHistoryJpaEntity.pointHistoryJpaEntity;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final JPAQueryFactory queryFactory;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public List<PointHistory> findByMemberIdOrderByCreatedAtDesc(MemberId memberId) {
        return queryFactory
            .selectFrom(pointHistoryJpaEntity)
            .where(pointHistoryJpaEntity.memberId.eq(memberId))
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(PointHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<PointHistory> findPointHistory(PointSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(pointHistoryJpaEntity.id.count())
            .from(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .fetchOne();

        List<PointHistory> content = queryFactory
            .selectFrom(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(PointHistoryMapper::toDomain)
            .collect(Collectors.toList());

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PointHistory save(PointHistory history) {
        PointHistoryJpaEntity saved = pointHistoryJpaRepository.save(PointHistoryMapper.toEntity(history));
        return PointHistoryMapper.toDomain(saved);
    }

    private BooleanExpression pointTypeEq(PointType pointType) {
        return pointType != null ? pointHistoryJpaEntity.pointType.eq(pointType) : null;
    }
}
