package com.tastyhouse.application.coupon.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.coupon.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.event.MemberCouponUsedEvent;
import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.CouponUseResult;
import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.coupon.port.out.write.CouponLoadPort;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponLoadPort;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class CouponIssueService {

    private final CouponLoadPort couponLoadPort;
    private final MemberCouponLoadPort memberCouponLoadPort;
    private final MemberCouponSavePort memberCouponSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public CouponIssueService(
        CouponLoadPort couponLoadPort,
        MemberCouponLoadPort memberCouponLoadPort,
        MemberCouponSavePort memberCouponSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.couponLoadPort = couponLoadPort;
        this.memberCouponLoadPort = memberCouponLoadPort;
        this.memberCouponSavePort = memberCouponSavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public MemberCouponId issueCoupon(MemberId memberId, CouponId couponId) {
        Coupon coupon = findCouponOrThrow(couponId);

        if (memberCouponLoadPort.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new ApplicationException(ApplicationErrorCode.COUPON_ALREADY_ISSUED);
        }

        MemberCoupon issued = memberCouponSavePort.save(
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
        MemberCoupon memberCoupon = memberCouponLoadPort.findById(memberCouponId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_COUPON_NOT_FOUND));

        if (!memberCoupon.getMemberId().equals(memberId)) {
            throw new ApplicationException(ApplicationErrorCode.COUPON_ACCESS_DENIED);
        }

        Coupon coupon = couponLoadPort.findById(memberCoupon.getCouponId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.COUPON_INFO_NOT_FOUND));

        coupon.validateMinOrderAmount(orderAmountAfterProductDiscount);
        int discountAmount = coupon.calculateDiscount(orderAmountAfterProductDiscount);

        memberCoupon.use();
        memberCouponSavePort.save(memberCoupon);

        domainEventPublisher.publish(new MemberCouponUsedEvent(
            memberCoupon.getMemberCouponId(),
            memberId,
            coupon.getCouponId(),
            LocalDateTime.now()
        ));

        return CouponUseResult.of(memberCoupon.getId(), discountAmount);
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponLoadPort.findById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }
}
