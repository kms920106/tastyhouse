package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

/**
 * 회원별 리뷰 수 집계 read 어댑터(CQRS query 측).
 *
 * <p>리뷰 도메인의 집계 조회지만 소비자는 리뷰 화면이 아니라 랭킹 집계와 회원 등급 산정이다. review
 * 도메인 전체 전환(`30-review`)은 아직 진행되지 않았으므로, 이번 rank 전환에 필요한 이 집계 조회만
 * 먼저 query 패키지로 분리한다. 나머지 리뷰 read model은 기존 {@code ReviewRepositoryImpl}에 잔류한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberReviewCountQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 기간 내 회원별 리뷰 수를 집계한다(리뷰 수 내림차순 → 마지막 작성 이른 순 → 회원 ID 순).
     *
     * <p>기간은 시작 시각 이상, 종료 시각 미만(반열림 구간)이다.
     */
    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        NumberPath<Long> memberIdPath = Expressions.numberPath(Long.class, reviewJpaEntity, "memberId");

        return queryFactory
            .select(new QMemberReviewCountResult(
                reviewJpaEntity.memberId,
                reviewJpaEntity.count(),
                reviewJpaEntity.createdAt.max()
            ))
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.createdAt.goe(startDate),
                reviewJpaEntity.createdAt.lt(endDate)
            )
            .groupBy(memberIdPath)
            .orderBy(
                reviewJpaEntity.count().desc(),
                reviewJpaEntity.createdAt.max().asc(),
                memberIdPath.asc()
            )
            .fetch();
    }
}
