package com.tastyhouse.webapi.member;

import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.coupon.MemberCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final MemberCouponJpaRepository memberCouponJpaRepository;

    public MemberCoupon findById(Long id) {
        return memberCouponJpaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_COUPON_NOT_FOUND));
    }
}
