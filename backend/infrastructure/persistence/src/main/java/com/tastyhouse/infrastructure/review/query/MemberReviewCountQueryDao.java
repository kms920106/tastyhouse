package com.tastyhouse.infrastructure.review.query;

import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.menureview.port.out.MenuReviewMemberCountResult;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewStatisticsQueryDao;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

@Repository
public class MemberReviewCountQueryDao implements MemberReviewCountQueryPort {
    private final JPAQueryFactory queryFactory;
    private final MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao;

    public MemberReviewCountQueryDao(
        JPAQueryFactory queryFactory,
        MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao
    ) {
        this.queryFactory = queryFactory;
        this.menuReviewStatisticsQueryDao = menuReviewStatisticsQueryDao;
    }

    @Override
    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return mergeAndSort(
            findReviewCounts(startDate, endDate),
            menuReviewStatisticsQueryDao.countByMemberWithPeriod(startDate, endDate)
        );
    }

    static List<MemberReviewCountResult> mergeAndSort(
        List<MemberReviewCountResult> reviewCounts,
        List<MenuReviewMemberCountResult> menuReviewCounts
    ) {
        Map<Long, MemberReviewCountResult> merged = new LinkedHashMap<>();

        for (MemberReviewCountResult row : reviewCounts) {
            merged.merge(row.memberId(), row, MemberReviewCountQueryDao::sum);
        }

        for (MenuReviewMemberCountResult row : menuReviewCounts) {
            merged.merge(
                row.memberId(),
                new MemberReviewCountResult(row.memberId(), row.menuReviewCount(), row.lastMenuReviewAt()),
                MemberReviewCountQueryDao::sum
            );
        }

        return merged.values().stream()
            .sorted(
                Comparator.comparing(MemberReviewCountResult::reviewCount, Comparator.reverseOrder())
                    .thenComparing(MemberReviewCountResult::lastReviewAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(MemberReviewCountResult::memberId)
            )
            .toList();
    }

    private List<MemberReviewCountResult> findReviewCounts(LocalDateTime startDate, LocalDateTime endDate) {
        NumberPath<Long> memberIdPath = reviewJpaEntity.memberId;

        return queryFactory
            .select(Projections.constructor(MemberReviewCountResult.class,
                memberIdPath,
                reviewJpaEntity.count(),
                reviewJpaEntity.createdAt.max()
            ))
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.createdAt.goe(startDate),
                reviewJpaEntity.createdAt.lt(endDate),
                reviewJpaEntity.ownerOnly.isFalse()
            )
            .groupBy(memberIdPath)
            .fetch();
    }

    private static MemberReviewCountResult sum(MemberReviewCountResult left, MemberReviewCountResult right) {
        return new MemberReviewCountResult(
            left.memberId(),
            nullToZero(left.reviewCount()) + nullToZero(right.reviewCount()),
            latest(left.lastReviewAt(), right.lastReviewAt())
        );
    }

    private static long nullToZero(Long count) {
        return count == null ? 0L : count;
    }

    private static LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }
}
