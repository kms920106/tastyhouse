package com.tastyhouse.adminapi.coupon;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 쿠폰 관리 명령 서비스(admin).
 *
 * <p>쿠폰 CRUD는 단일 애그리거트({@code Coupon}) 조작이므로 write 포트를 직접 주입해 이 서비스가
 * 처리하고(분류 A), 회원 발급은 쿠폰·회원 쿠폰 두 애그리거트에 걸친 불변식이므로 도메인 서비스
 * {@link CouponIssueService}에 위임한다.
 *
 * <p>{@code Coupon}은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 * HTTP 경계에서 받은 {@code Long}·{@code String}은 이 계층에서 {@code CouponId}·{@code DiscountType}으로
 * 승격한다.
 */
@Service
@Transactional
public class CouponCommandService {

    private final CouponRepository couponRepository;
    private final CouponIssueService couponIssueService;

    public CouponCommandService(CouponRepository couponRepository, CouponIssueService couponIssueService) {
        this.couponRepository = couponRepository;
        this.couponIssueService = couponIssueService;
    }

    public Long createCoupon(
        String name,
        String description,
        String discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        Coupon coupon = Coupon.of(
            name,
            description,
            DiscountType.from(discountType),
            discountAmount,
            maxDiscountAmount,
            minOrderAmount,
            maxDiscountCount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt,
            visible
        );
        Coupon saved = couponRepository.save(coupon);
        return saved.getCouponId().value();
    }

    public void updateCoupon(
        Long id,
        String name,
        String description,
        String discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        CouponId couponId = CouponId.of(id);
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.update(
            name,
            description,
            DiscountType.from(discountType),
            discountAmount,
            maxDiscountAmount,
            minOrderAmount,
            maxDiscountCount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt,
            visible
        );
        couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id) {
        CouponId couponId = CouponId.of(id);
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.delete();
        couponRepository.save(coupon);
    }

    /**
     * 특정 회원에게 쿠폰을 수동 발급한다. 만료 일시는 쿠폰의 사용 종료 일시를 승계하며, 중복 보유
     * 검증까지 도메인 서비스가 수행한다.
     */
    public Long issueCoupon(Long id, Long memberId) {
        CouponId couponId = CouponId.of(id);
        MemberId targetMemberId = MemberId.of(memberId);
        return couponIssueService.issueCoupon(targetMemberId, couponId).value();
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }
}
