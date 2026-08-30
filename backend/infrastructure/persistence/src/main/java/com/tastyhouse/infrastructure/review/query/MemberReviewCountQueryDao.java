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

/**
 * 회원별 리뷰 수 집계 read 어댑터(CQRS query 측).
 *
 * <p>리뷰 도메인의 집계 조회지만 소비자는 리뷰 화면이 아니라 랭킹 집계와 회원 등급 산정이다. 어댑터 2개
 * ({@code MemberReviewCountAdapter}·{@code MemberGradeReviewCountAdapter})가 이 DAO 하나를 공유하므로,
 * 여기만 고치면 랭킹과 등급이 함께 같은 기준으로 전환된다.
 */
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

    /**
     * 기간 내 회원별 <b>매장 리뷰 + 메뉴 평가</b> 합산 건수를 집계한다
     * (건수 내림차순 → 마지막 작성 이른 순 → 회원 ID 순).
     *
     * <p>기간은 시작 시각 이상, 종료 시각 미만(반열림 구간)이다.
     *
     * <p><b>구현이 union이 아니라 "두 쿼리 fetch 후 Java 병합"인 이유</b>: QueryDSL의 union은
     * {@code groupBy}/{@code orderBy} 조합에서 제약이 많고, 두 결과를 {@code Map}으로 병합하는 편이 위
     * 3단 정렬 규칙을 명시적으로 재현하기 쉽다.
     *
     * <p><b>병합 규칙</b> — 건수는 더하고, {@code lastReviewAt}은 두 값 중 <b>더 늦은 쪽</b>을 취한다.
     * 이걸 빠뜨리면 동점자 정렬이 어긋난다.
     *
     * <p><b>어뷰징 방지 필터는 양쪽에 대칭으로 건다.</b>
     * <ul>
     *   <li>REVIEW — {@code ownerOnly.isFalse()}. 비공개 리뷰를 양산해 랭킹을 올리는 경로를 막는다
     *       (비공개라 신고·모니터링 대상도 되지 않는다).</li>
     *   <li>MENU_REVIEW — {@code hidden.isFalse()}. MENU_REVIEW에는 {@code ownerOnly} 개념이 없다.</li>
     * </ul>
     *
     * <p><b>⚠️ 반면 REVIEW의 {@code hidden}(관리자 게시중단) 필터는 의도적으로 걸지 않는다 — 기존 동작
     * 유지이며 별도 과제다.</b> 지금 추가하면 이미 산정된 기존 회원들의 랭킹 순위가 실제로 바뀌므로,
     * MENU_REVIEW 쪽에는 {@code hidden} 필터가 있는 것과 비대칭으로 보여도 <b>고치지 말 것</b>.
     * MENU_REVIEW는 신규 테이블이라 소급 영향이 없어 처음부터 걸 수 있었을 뿐이다.
     */
    @Override
    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return mergeAndSort(
            findReviewCounts(startDate, endDate),
            menuReviewStatisticsQueryDao.countByMemberWithPeriod(startDate, endDate)
        );
    }

    /**
     * 두 집계를 회원 단위로 병합하고 랭킹 정렬 규칙을 적용한다.
     *
     * <p>쿼리와 분리해 package-private으로 둔 이유는 <b>테스트 때문</b>이다 — 병합·정렬은 순수 함수인데
     * 쿼리에 묶여 있으면 DB 없이는 검증할 수 없고, 도메인 서비스 테스트는 포트를 fake로 쓰므로 이 변경을
     * 아예 잡지 못한다.
     */
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

    /**
     * 기간 내 회원별 매장 리뷰 수 — 정렬은 병합 이후에 하므로 여기서는 그룹 집계만 한다.
     */
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

    /**
     * 같은 회원의 두 집계를 합친다 — 건수는 더하고 마지막 작성 시각은 더 늦은 쪽을 취한다.
     */
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
