package com.tastyhouse.core.domain.coupon.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.coupon.domain.event.MemberCouponIssuedEvent;
import com.tastyhouse.core.domain.coupon.domain.event.MemberCouponUsedEvent;
import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponCreateCommand;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponUpdateCommand;
import com.tastyhouse.core.domain.coupon.application.dto.command.IssueCouponCommand;
import com.tastyhouse.core.domain.coupon.application.dto.command.UseCouponCommand;
import com.tastyhouse.core.domain.coupon.application.dto.result.UseCouponResult;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponCommandService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CouponId createCoupon(CouponCreateCommand command) {
        Coupon coupon = Coupon.of(
            command.name(),
            command.description(),
            command.discountType(),
            command.discountAmount(),
            command.maxDiscountAmount(),
            command.minOrderAmount(),
            command.maxDiscountCount(),
            command.issueStartAt(),
            command.issueEndAt(),
            command.useStartAt(),
            command.useEndAt(),
            command.visible()
        );
        Coupon saved = couponRepository.save(coupon);
        return saved.getCouponId();
    }

    public void updateCoupon(CouponId couponId, CouponUpdateCommand command) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        coupon.update(
            command.name(),
            command.description(),
            command.discountType(),
            command.discountAmount(),
            command.maxDiscountAmount(),
            command.minOrderAmount(),
            command.maxDiscountCount(),
            command.issueStartAt(),
            command.issueEndAt(),
            command.useStartAt(),
            command.useEndAt(),
            command.visible()
        );
        couponRepository.save(coupon);
    }

    public void deleteCoupon(CouponId couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        coupon.delete();
        couponRepository.save(coupon);
    }

    public MemberCouponId issueCouponByAdmin(CouponId couponId, MemberId memberId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        IssueCouponCommand command = IssueCouponCommand.of(memberId, couponId.value(), coupon.getUseEndAt());
        return issueCoupon(command);
    }

    public MemberCouponId issueCoupon(IssueCouponCommand command) {
        MemberCoupon issued = memberCouponRepository.save(
            MemberCoupon.of(
                command.memberId(),
                command.couponId().value(),
                false,
                null,
                command.expiredAt()
            )
        );
        MemberCouponId memberCouponId = issued.getMemberCouponId();
        eventPublisher.publishEvent(new MemberCouponIssuedEvent(
            memberCouponId,
            command.memberId(),
            command.couponId(),
            LocalDateTime.now()
        ));
        return memberCouponId;
    }

    public UseCouponResult useCoupon(UseCouponCommand command) {
        MemberCoupon memberCoupon = memberCouponRepository.findById(command.memberCouponId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_COUPON_NOT_FOUND));

        if (!memberCoupon.getMemberId().equals(command.memberId())) {
            throw new AccessDeniedException(ErrorCode.COUPON_ACCESS_DENIED);
        }

        Coupon coupon = couponRepository.findById(CouponId.of(memberCoupon.getCouponId()))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_INFO_NOT_FOUND));

        coupon.validateMinOrderAmount(command.orderAmountAfterProductDiscount());
        int discount = coupon.calculateDiscount(command.orderAmountAfterProductDiscount());

        memberCoupon.use();
        memberCouponRepository.save(memberCoupon);

        eventPublisher.publishEvent(new MemberCouponUsedEvent(
            memberCoupon.getMemberCouponId(),
            command.memberId(),
            coupon.getCouponId(),
            LocalDateTime.now()
        ));

        return new UseCouponResult(memberCoupon.getId(), discount);
    }
}
