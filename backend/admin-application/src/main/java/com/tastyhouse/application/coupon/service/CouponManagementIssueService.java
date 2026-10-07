package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.coupon.port.in.CouponIssueCommand;
import com.tastyhouse.application.coupon.port.in.CouponManagementIssueUseCase;

@Service
@Transactional
class CouponManagementIssueService implements CouponManagementIssueUseCase {

    private final CouponIssueService couponIssueService;

    public CouponManagementIssueService(CouponIssueService couponIssueService) {
        this.couponIssueService = couponIssueService;
    }

    @Override
    public Long issueCoupon(CouponIssueCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        MemberId targetMemberId = MemberId.of(command.memberId());
        return couponIssueService.issueCoupon(targetMemberId, couponId).value();
    }
}
