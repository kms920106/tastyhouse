package com.tastyhouse.infrastructure.point.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.point.persistence.QPointHistoryJpaEntity.pointHistoryJpaEntity;
import static com.tastyhouse.infrastructure.point.persistence.QPointJpaEntity.pointJpaEntity;

/**
 * 포인트 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code PointRepository}/{@code PointHistoryRepository})와 역할이 겹치지 않는다. 소비 모듈
 * (web-api/admin-api)의 {@code PointQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은
 * QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에는 admin 마커를
 * 붙이지 않고 순수 동작명을 쓰며, 전체 이력({@code findPointHistories})과 페이징 검색
 * ({@code findPointHistoryPage})은 시그니처로 구분한다.
 */
@Repository
public class PointQueryDao {

    private final JPAQueryFactory queryFactory;

    public PointQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 회원 포인트 잔액 조회 — 포인트 계정이 없는 회원이면 비어 있다(소비 측에서 0으로 대체).
     */
    public Optional<PointBalanceResult> findBalanceByMemberId(MemberId memberId) {
        PointBalanceResult balance = queryFactory
            .select(new QPointBalanceResult(
                pointJpaEntity.availablePoints,
                pointJpaEntity.expiredThisMonth
            ))
            .from(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId))
            .fetchOne();

        return Optional.ofNullable(balance);
    }

    /**
     * 회원 포인트 이력 전체 조회(최신순) — web의 내 포인트 내역 화면이 페이징 없이 전체를 소비한다.
     */
    public List<PointHistoryResult> findPointHistories(MemberId memberId) {
        return queryFactory
            .select(new QPointHistoryResult(
                pointHistoryJpaEntity.pointType,
                pointHistoryJpaEntity.pointAmount,
                pointHistoryJpaEntity.reason,
                pointHistoryJpaEntity.createdAt
            ))
            .from(pointHistoryJpaEntity)
            .where(pointHistoryJpaEntity.memberId.eq(memberId))
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .fetch();
    }

    /**
     * 회원 포인트 이력 페이징 조회(최신순) — 유형(pointType) 필터를 선택적으로 적용한다.
     */
    public PageResult<PointHistoryResult> findPointHistoryPage(PointSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(pointHistoryJpaEntity.id.count())
            .from(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .fetchOne();

        List<PointHistoryResult> content = queryFactory
            .select(new QPointHistoryResult(
                pointHistoryJpaEntity.pointType,
                pointHistoryJpaEntity.pointAmount,
                pointHistoryJpaEntity.reason,
                pointHistoryJpaEntity.createdAt
            ))
            .from(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression pointTypeEq(PointType pointType) {
        return pointType != null ? pointHistoryJpaEntity.pointType.eq(pointType) : null;
    }
}
