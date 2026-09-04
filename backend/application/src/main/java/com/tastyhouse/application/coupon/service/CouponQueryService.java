package com.tastyhouse.application.coupon.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.coupon.port.out.CouponQueryPort;
import com.tastyhouse.application.coupon.port.out.MemberCouponResult;
import com.tastyhouse.application.coupon.port.in.CouponQueryUseCase;
import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

/**
 * 내 쿠폰 조회 서비스(web).
 *
 * <p>읽기 포트({@link CouponQueryPort})만 주입해 조회한다. web-api에는 쿠폰
 * 쓰기 경로가 없으므로(주문 결제 사용은 order 도메인 트랜잭션 안에서 도메인 서비스가 처리, 관리자 발급은
 * admin-api 담당) CommandService를 두지 않는다 — point 도메인과 동일한 형태다.
 *
 * <p>만료 파생값({@code daysRemaining}·{@code expired})은 <b>조회 시각 기준 계산</b>이라 이 서비스가
 * 끝낸다(챕터 10) — 응답 조립은 web-api의 {@code MyCouponListItemResponse}가 담당하며 필드 복사만 한다.
 * 응답 record가 시계를 읽으면 조립이 비결정 연산을 품게 된다.
 */
@Service
@WebApp
@Transactional(readOnly = true)
public class CouponQueryService implements CouponQueryUseCase {

    private final CouponQueryPort couponQueryPort;

    public CouponQueryService(CouponQueryPort couponQueryPort) {
        this.couponQueryPort = couponQueryPort;
    }

    /**
     * 내 쿠폰함 — 사용·만료분까지 전부 조회한다.
     */
    @Override
    public List<MyCouponListItemResult> getMyCoupons(Long memberId) {
        return couponQueryPort.findMemberCoupons(memberId)
            .stream()
            .map(this::toMyCouponListItemResult)
            .toList();
    }

    /**
     * 지금 사용할 수 있는 내 쿠폰만 조회한다(주문 화면 쿠폰 선택).
     */
    @Override
    public List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId) {
        return couponQueryPort.findAvailableMemberCoupons(memberId, LocalDateTime.now())
            .stream()
            .map(this::toMyCouponListItemResult)
            .toList();
    }

    /**
     * 읽기 포트 투영에 만료 파생값을 얹어 목록 항목 계약을 완성한다.
     *
     * <p>파생 규칙은 승격 전 {@code MyCouponListItemResponse.of(...)}의 계산을 그대로 옮긴 것이다 —
     * {@code daysRemaining}은 미사용·만료 전일 때만 남은 일수이고 그 밖에는 null, {@code expired}는
     * {@code expiredAt}이 있고 이미 지났을 때만 true.
     */
    private MyCouponListItemResult toMyCouponListItemResult(MemberCouponResult dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = dto.expiredAt();
        boolean used = dto.used();

        Long daysRemaining = null;
        if (!used && expiredAt != null && now.isBefore(expiredAt)) {
            daysRemaining = ChronoUnit.DAYS.between(now, expiredAt);
        }

        boolean expired = expiredAt != null && now.isAfter(expiredAt);

        return new MyCouponListItemResult(
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
            expiredAt,
            used,
            dto.usedAt(),
            daysRemaining,
            expired
        );
    }
}
