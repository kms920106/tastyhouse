package com.tastyhouse.domain.coupon.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.event.MemberCouponUsedEvent;
import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

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
