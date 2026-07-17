package com.tastyhouse.webapi.member;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.CouponQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.member.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.response.MemberPointHistoryResponse;
import com.tastyhouse.webapi.member.response.MemberPointResponse;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.MemberUsablePointResponse;
import com.tastyhouse.webapi.member.response.MemberVerifyPasswordResponse;
import com.tastyhouse.webapi.member.response.MyCouponListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyProfileResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;
import com.tastyhouse.webapi.member.service.MemberAccountService;
import com.tastyhouse.webapi.member.service.MemberAuthService;
import com.tastyhouse.webapi.member.service.MemberFollowService;
import com.tastyhouse.webapi.member.service.MemberGradeService;
import com.tastyhouse.webapi.member.service.MemberPointService;
import com.tastyhouse.webapi.member.service.MemberReviewService;
import com.tastyhouse.webapi.member.service.MemberShopService;

@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberAccountService memberAccountService;
    private final MemberAuthService memberAuthService;
    private final MemberFollowService memberFollowService;
    private final MemberPointService memberPointService;
    private final MemberShopService memberShopService;
    private final MemberReviewService memberReviewService;
    private final CouponQueryService couponQueryService;
    private final MemberGradeService memberGradeService;

    public void updateMyProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        memberAccountService.updateMemberProfile(memberId, nickname, statusMessage, profileImageFileId);
    }

    public MemberVerifyPasswordResponse verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        String verifyToken = memberAuthService.createPersonalInfoVerifyToken(memberId);
        return MemberVerifyPasswordResponse.from(verifyToken);
    }

    public MemberPersonalInfoResponse getPersonalInfo(Long memberId) {
        return memberAccountService.getPersonalInfo(memberId);
    }

    public void updatePersonalInfo(Long memberId, String verifyToken,
                                   String phoneVerifyToken, String fullName,
                                   String phoneNumber, Integer birthDate, String gender,
                                   boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                                   boolean eventInfoEnabled) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, phoneVerifyToken, phoneNumber);
        }
        memberAccountService.updatePersonalInfo(memberId, fullName, phoneNumber, birthDate,
            gender == null ? null : MemberGender.from(gender),
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    public void updatePassword(Long memberId, String verifyToken, String newPassword, String newPasswordConfirm) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        memberAuthService.verifyNotSamePassword(memberId, newPassword);
        memberAccountService.updatePassword(memberId, newPassword, newPasswordConfirm);
    }

    public void withdrawMember(Long memberId, String reason, String reasonDetail, String bearerToken) {
        memberAccountService.withdrawMember(memberId, MemberWithdrawalReason.from(reason), reasonDetail);
        memberAuthService.invalidateAccessToken(bearerToken);
    }

    public MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        return memberAccountService.checkNicknameAvailability(nickname);
    }

    public MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        return memberAccountService.checkPhoneAvailability(phoneNumber);
    }

    public MyGradeResponse getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }

    public MemberPointResponse getMyPoint(Long memberId) {
        return memberPointService.getMemberPoint(memberId);
    }

    public MemberPointHistoryResponse getMyPointHistory(Long memberId) {
        return memberPointService.getPointHistory(memberId);
    }

    public MemberUsablePointResponse getMyUsablePoint(Long memberId) {
        return memberPointService.getUsablePoint(memberId);
    }

    public List<MyCouponListItemResponse> getMyCoupons(Long memberId) {
        return couponQueryService.findMemberCoupons(MemberId.of(memberId)).stream()
            .map(r -> MyCouponListItemResponse.of(
                r.id(), r.couponId(), r.name(), r.description(),
                r.discountType().name(), r.discountAmount(), r.maxDiscountAmount(),
                r.minOrderAmount(), r.useStartAt(), r.useEndAt(),
                r.expiredAt(), r.used(), r.usedAt()
            ))
            .toList();
    }

    public List<MyCouponListItemResponse> getMyAvailableCoupons(Long memberId) {
        return couponQueryService.findAvailableMemberCoupons(MemberId.of(memberId)).stream()
            .map(r -> MyCouponListItemResponse.of(
                r.id(), r.couponId(), r.name(), r.description(),
                r.discountType().name(), r.discountAmount(), r.maxDiscountAmount(),
                r.minOrderAmount(), r.useStartAt(), r.useEndAt(),
                r.expiredAt(), r.used(), r.usedAt()
            ))
            .toList();
    }

    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        return memberReviewService.getMyReviews(memberId, page, size);
    }

    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        return memberReviewService.getMyReviewCount(memberId);
    }

    public PageResult<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
        return memberShopService.getMyBookmarkedShops(memberId, page, size);
    }

    public MemberProfileResponse getMemberBasicProfile(Long targetMemberId) {
        return memberAccountService.getMemberProfile(targetMemberId);
    }

    public MyProfileResponse getMyProfile(Long memberId) {
        return memberAccountService.getMyProfile(memberId);
    }

    public MemberStatsResponse getMemberStats(Long memberId) {
        return memberFollowService.getMemberStats(memberId);
    }
}
