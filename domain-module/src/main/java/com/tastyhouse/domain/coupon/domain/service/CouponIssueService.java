package com.tastyhouse.domain.coupon.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.domain.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.domain.event.MemberCouponUsedEvent;
import com.tastyhouse.domain.coupon.domain.model.Coupon;
import com.tastyhouse.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 쿠폰 발급·사용(도메인 서비스).
 *
 * <p>발급과 사용 모두 {@code Coupon}(쿠폰 원본 정책)과 {@code MemberCoupon}(회원 보유분) 두 애그리거트를
 * 한 트랜잭션에서 함께 load &amp; save 하는 원자 연산이다. 발급은 쿠폰 존재·중복 보유 여부를 검증한 뒤
 * 쿠폰의 사용 종료 일시를 회원 쿠폰 만료 일시로 승계하고, 사용은 소유권·최소 주문금액을 검증한 뒤 원본
 * 쿠폰의 할인 정책으로 할인액을 산출하면서 회원 쿠폰을 사용 상태로 전이시킨다. 애그리거트 타입 2개에
 * 걸친 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 두어, 트리거 경로(관리자 수동 발급·이벤트 반응
 * 자동 발급·주문 결제 사용)가 여러 개여도 "발급/사용 규칙"이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스 또는 infrastructure의 이벤트 리스너가 선언한다.
 *
 * <p>{@code Coupon}/{@code MemberCoupon}은 순수 POJO라 더티 체킹이 없으므로 상태 전이 후 명시적으로
 * {@code save}를 호출한다.
 */
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CouponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.couponRepository = couponRepository;
        this.memberCouponRepository = memberCouponRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 회원에게 쿠폰을 발급한다. 쿠폰이 없으면 {@code COUPON_NOT_FOUND}, 이미 같은 쿠폰을 보유하고 있으면
     * {@code COUPON_ALREADY_ISSUED}로 실패한다. 회원 쿠폰의 만료 일시는 쿠폰의 사용 종료 일시를 승계한다.
     */
    public MemberCouponId issueCoupon(MemberId memberId, CouponId couponId) {
        Coupon coupon = findCouponOrThrow(couponId);

        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        MemberCoupon issued = memberCouponRepository.save(
            MemberCoupon.of(memberId, couponId, false, null, coupon.getUseEndAt())
        );
        MemberCouponId memberCouponId = issued.getMemberCouponId();

        domainEventPublisher.publish(new MemberCouponIssuedEvent(
            memberCouponId,
            memberId,
            couponId,
            LocalDateTime.now()
        ));

        return memberCouponId;
    }

    /**
     * 보유한 쿠폰을 사용 처리하고 할인액을 산출한다. 본인 쿠폰이 아니면 {@code COUPON_ACCESS_DENIED},
     * 이미 사용했거나 만료됐으면 {@code COUPON_NOT_AVAILABLE}, 최소 주문 금액에 미달하면 쿠폰 정책이
     * 정한 예외로 실패한다.
     *
     * @param orderAmountAfterProductDiscount 상품 할인까지 적용된 주문 금액(쿠폰 할인 산출 기준액)
     */
    public CouponUseResult useCoupon(MemberCouponId memberCouponId, MemberId memberId, int orderAmountAfterProductDiscount) {
        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_COUPON_NOT_FOUND));

        if (!memberCoupon.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COUPON_ACCESS_DENIED);
        }

        Coupon coupon = couponRepository.findById(memberCoupon.getCouponId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_INFO_NOT_FOUND));

        coupon.validateMinOrderAmount(orderAmountAfterProductDiscount);
        int discountAmount = coupon.calculateDiscount(orderAmountAfterProductDiscount);

        memberCoupon.use();
        memberCouponRepository.save(memberCoupon);

        domainEventPublisher.publish(new MemberCouponUsedEvent(
            memberCoupon.getMemberCouponId(),
            memberId,
            coupon.getCouponId(),
            LocalDateTime.now()
        ));

        return CouponUseResult.of(memberCoupon.getId(), discountAmount);
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }
}
