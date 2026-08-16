package com.tastyhouse.infrastructure.menureview.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

/**
 * 메뉴 평가 집계 전용 read 어댑터(CQRS query 측).
 *
 * <p>두 소비자가 있다.
 * <ul>
 *   <li><b>상품 평점 재집계</b> — {@code ProductReviewStatisticsAdapter}가 도메인 포트
 *       {@code ProductReviewStatisticsPort}를 구현하며 여기에 위임한다. {@code PRODUCT.rating}의
 *       <b>유일한 근거</b>가 이 집계다(과거의 REVIEW 기준 집계는 이관되어 사라졌다).</li>
 *   <li><b>랭킹·회원등급 기간 집계</b> — {@code MemberReviewCountQueryDao}가 REVIEW 집계와 합산한다.</li>
 * </ul>
 *
 * <p><b>고객 노출 조건은 {@code hidden = false} 하나뿐이다</b> — MENU_REVIEW에는 사장님만보기
 * ({@code ownerOnly}) 개념이 없다. 매장 리뷰 집계({@code ReviewStatisticsQueryDao})가 두 축을 함께 거는
 * 것과 다르므로, 두 DAO를 비교하며 "필터가 누락됐다"고 오해하지 말 것.
 */
@Repository
public class MenuReviewStatisticsQueryDao {

    private final JPAQueryFactory queryFactory;

    public MenuReviewStatisticsQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 상품의 고객 노출 메뉴 평가 수(숨김 제외).
     */
    public Long countVisibleByProductId(Long productId) {
        return queryFactory
            .select(menuReviewJpaEntity.count())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();
    }

    /**
     * 상품의 고객 노출 메뉴 평가 평균 평점(숨김 제외). 대상이 없으면 {@code null}이다
     * ("평점 0점"과 구분해야 하므로 0.0이 아니다).
     */
    public Double getAverageRatingByProductId(Long productId) {
        return queryFactory
            .select(menuReviewJpaEntity.rating.avg())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();
    }

    /**
     * 기간 내 회원별 메뉴 평가 수 집계 — 반열림 구간 {@code [startDate, endDate)}.
     *
     * <p>어뷰징 방지 필터로 {@code hidden = false}를 건다. 매장 리뷰 쪽의 {@code ownerOnly} 필터와
     * 대칭인 자리이며, MENU_REVIEW에는 사장님만보기 개념이 없어 숨김만 거른다.
     *
     * <p>정렬은 하지 않는다 — 소비 측({@code MemberReviewCountQueryDao})이 REVIEW 집계와 병합한 <b>뒤에</b>
     * 정렬해야 하므로, 여기서 정렬해도 그 결과가 유지되지 않는다.
     */
    public List<MenuReviewMemberCountResult> countByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        NumberPath<Long> memberIdPath = menuReviewJpaEntity.memberId;

        return queryFactory
            .select(new QMenuReviewMemberCountResult(
                memberIdPath,
                menuReviewJpaEntity.count(),
                menuReviewJpaEntity.createdAt.max()
            ))
            .from(menuReviewJpaEntity)
            .where(
                menuReviewJpaEntity.createdAt.goe(startDate),
                menuReviewJpaEntity.createdAt.lt(endDate),
                menuReviewJpaEntity.hidden.isFalse()
            )
            .groupBy(memberIdPath)
            .fetch();
    }
}
