package com.tastyhouse.infrastructure.coupon.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shared.query.ConvertedIdPaths;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;
import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

/**
 * 쿠폰 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code CouponRepository}/{@code MemberCouponRepository})와 역할이 겹치지 않는다. 소비 모듈
 * (web-api/admin-api)의 {@code CouponQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은
 * QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에는 admin 마커를
 * 붙이지 않고 순수 동작명을 쓰며, admin의 쿠폰 관리 목록({@code findAllCoupons})과 web의 내 쿠폰 목록
 * ({@code findMemberCoupons}/{@code findAvailableMemberCoupons})은 시그니처로 구분한다.
 *
 * <p>삭제된 쿠폰(soft delete)은 admin 목록·상세에서 제외한다. 내 쿠폰 조회는 이관 이전 동작을 그대로
 * 보존해 원본 쿠폰의 삭제 여부를 필터링하지 않는다(이미 발급된 보유분은 계속 보인다).
 */
@Repository
public class CouponQueryDao {

    private final JPAQueryFactory queryFactory;

    public CouponQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 쿠폰 목록 페이징 조회(admin) — 쿠폰명 부분일치·할인유형·노출여부 필터를 선택적으로 적용한다.
     */
    public PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(couponJpaEntity.id.count())
            .from(couponJpaEntity)
            .where(
                couponJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<CouponListItemResult> content = queryFactory
            .select(new QCouponListItemResult(
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.maxDiscountCount,
                couponJpaEntity.issueStartAt,
                couponJpaEntity.issueEndAt,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                couponJpaEntity.visible
            ))
            .from(couponJpaEntity)
            .where(
                couponJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .orderBy(couponJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 쿠폰 상세 조회(admin) — 삭제된 쿠폰이면 비어 있다(소비 측에서 404로 변환).
     */
    public Optional<CouponDetailResult> findCouponDetailById(CouponId couponId) {
        CouponDetailResult detail = queryFactory
            .select(new QCouponDetailResult(
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.description,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.maxDiscountCount,
                couponJpaEntity.issueStartAt,
                couponJpaEntity.issueEndAt,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                couponJpaEntity.visible,
                couponJpaEntity.createdAt,
                couponJpaEntity.updatedAt
            ))
            .from(couponJpaEntity)
            .where(couponJpaEntity.id.eq(couponId.value()), couponJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    /**
     * 특정 쿠폰의 회원 발급 현황 페이징 조회(admin).
     */
    public PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(memberCouponJpaEntity.id.count())
            .from(memberCouponJpaEntity)
            .where(ConvertedIdPaths.eq(memberCouponJpaEntity, "couponId", CouponId.class, CouponId::of, couponId.value()))
            .fetchOne();

        List<MemberCouponItemResult> content = queryFactory
            .select(new QMemberCouponItemResult(
                memberCouponJpaEntity.id,
                memberCouponJpaEntity.memberId,
                memberCouponJpaEntity.used,
                memberCouponJpaEntity.usedAt,
                memberCouponJpaEntity.expiredAt,
                memberCouponJpaEntity.createdAt
            ))
            .from(memberCouponJpaEntity)
            .where(ConvertedIdPaths.eq(memberCouponJpaEntity, "couponId", CouponId.class, CouponId::of, couponId.value()))
            .orderBy(memberCouponJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 회원이 보유한 쿠폰 전체 조회(web 내 쿠폰함) — 사용·만료분도 함께 보여준다.
     */
    public List<MemberCouponResult> findMemberCoupons(MemberId memberId) {
        return selectMemberCoupons()
            .where(memberCouponJpaEntity.memberId.eq(memberId))
            .fetch();
    }

    /**
     * 회원이 지금 사용할 수 있는 쿠폰만 조회(web 주문 화면의 쿠폰 선택) — 미사용 &amp; 미만료분.
     */
    public List<MemberCouponResult> findAvailableMemberCoupons(MemberId memberId, LocalDateTime now) {
        return selectMemberCoupons()
            .where(
                memberCouponJpaEntity.memberId.eq(memberId),
                memberCouponJpaEntity.used.isFalse(),
                memberCouponJpaEntity.expiredAt.gt(now)
            )
            .fetch();
    }

    /**
     * 내 쿠폰 목록 두 메서드가 공유하는 투영·조인 — where 절만 각자 덧붙인다.
     */
    private JPAQuery<MemberCouponResult> selectMemberCoupons() {
        return queryFactory
            .select(new QMemberCouponResult(
                memberCouponJpaEntity.id,
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.description,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                memberCouponJpaEntity.expiredAt,
                memberCouponJpaEntity.used,
                memberCouponJpaEntity.usedAt
            ))
            .from(memberCouponJpaEntity)
            .join(couponJpaEntity).on(couponJpaEntity.id.eq(memberCouponIdCouponIdPath()));
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code MEMBER_COUPON.coupon_id}를 raw {@code Long}으로 비교·조인하기
     * 위한 path.
     */
    private NumberPath<Long> memberCouponIdCouponIdPath() {
        return Expressions.numberPath(Long.class, memberCouponJpaEntity, "couponId");
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? couponJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression discountTypeEq(DiscountType discountType) {
        return discountType != null ? couponJpaEntity.discountType.eq(discountType) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? couponJpaEntity.visible.eq(visible) : null;
    }
}
