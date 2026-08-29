package com.tastyhouse.webapi.coupon;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.coupon.query.CouponQueryDao;
import com.tastyhouse.infrastructure.coupon.query.MemberCouponResult;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyCouponListItemResponse;

/**
 * 내 쿠폰 조회 서비스(web).
 *
 * <p>infra read 어댑터({@link CouponQueryDao})만 주입해 조회하고 Response를 조립한다. web-api에는 쿠폰
 * 쓰기 경로가 없으므로(주문 결제 사용은 order 도메인 트랜잭션 안에서 도메인 서비스가 처리, 관리자 발급은
 * admin-api 담당) CommandService를 두지 않는다 — point 도메인과 동일한 형태다.
 *
 * <p>응답 record {@code MyCouponListItemResponse}는 내 정보 화면 응답 묶음의 일부라 기존 위치
 * ({@code webapi.member.response})를 그대로 유지한다.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponQueryDao couponQueryDao;

    public CouponQueryService(CouponQueryDao couponQueryDao) {
        this.couponQueryDao = couponQueryDao;
    }

    /**
     * 내 쿠폰함 — 사용·만료분까지 전부 조회한다.
     */
    public List<MyCouponListItemResponse> getMyCoupons(Long memberId) {
        return couponQueryDao.findMemberCoupons(memberId)
            .stream()
            .map(this::toMyCouponListItemResponse)
            .toList();
    }

    /**
     * 지금 사용할 수 있는 내 쿠폰만 조회한다(주문 화면 쿠폰 선택).
     */
    public List<MyCouponListItemResponse> getMyAvailableCoupons(Long memberId) {
        return couponQueryDao.findAvailableMemberCoupons(memberId, LocalDateTime.now())
            .stream()
            .map(this::toMyCouponListItemResponse)
            .toList();
    }

    private MyCouponListItemResponse toMyCouponListItemResponse(MemberCouponResult dto) {
        return MyCouponListItemResponse.of(
            dto.id(),
            dto.couponId(),
            dto.name(),
            dto.description(),
            dto.discountType().name(),
            dto.discountAmount(),
            dto.maxDiscountAmount(),
            dto.minOrderAmount(),
            dto.useStartAt(),
            dto.useEndAt(),
            dto.expiredAt(),
            dto.used(),
            dto.usedAt()
        );
    }
}
