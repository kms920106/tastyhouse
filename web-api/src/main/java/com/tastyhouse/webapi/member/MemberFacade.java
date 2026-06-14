package com.tastyhouse.webapi.member;

import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.member.domain.model.Gender;
import com.tastyhouse.core.domain.member.domain.model.WithdrawalReason;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MyProfileResponse;
import com.tastyhouse.core.domain.coupon.application.CouponQueryService;
import com.tastyhouse.webapi.member.response.MemberCouponListItemResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;
import com.tastyhouse.webapi.member.response.VerifyPasswordResponse;
import com.tastyhouse.webapi.member.service.MemberAccountService;
import com.tastyhouse.webapi.member.service.MemberAuthService;
import com.tastyhouse.webapi.member.service.MemberFollowService;
import com.tastyhouse.webapi.member.service.MemberGradeService;
import com.tastyhouse.webapi.member.service.MemberShopService;
import com.tastyhouse.webapi.member.service.MemberPointService;
import com.tastyhouse.webapi.member.service.MemberReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public VerifyPasswordResponse verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        String verifyToken = memberAuthService.createPersonalInfoVerifyToken(memberId);
        return VerifyPasswordResponse.from(verifyToken);
    }

    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        return memberAccountService.getPersonalInfo(memberId);
    }

    public void updatePersonalInfo(Long memberId, String verifyToken,
                                   String phoneVerifyToken, String fullName,
                                   String phoneNumber, Integer birthDate, Gender gender,
                                   Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                                   Boolean eventInfoEnabled) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, phoneVerifyToken, phoneNumber);
        }
        memberAccountService.updatePersonalInfo(memberId, fullName, phoneNumber, birthDate, gender,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    public void updatePassword(Long memberId, String verifyToken, String newPassword, String newPasswordConfirm) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        memberAuthService.verifyNotSamePassword(memberId, newPassword);
        memberAccountService.updatePassword(memberId, newPassword, newPasswordConfirm);
    }

    public void withdrawMember(Long memberId, WithdrawalReason reason, String reasonDetail, String bearerToken) {
        memberAccountService.withdrawMember(memberId, reason, reasonDetail);
        memberAuthService.invalidateAccessToken(bearerToken);
    }

    public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        return memberAccountService.checkNicknameAvailability(nickname);
    }

    public PhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        return memberAccountService.checkPhoneAvailability(phoneNumber);
    }

    public MyGradeResponse getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }

    public PointResponse getMyPoint(Long memberId) {
        return memberPointService.getMemberPoint(memberId);
    }

    public PointHistoryResponse getMyPointHistory(Long memberId) {
        return memberPointService.getPointHistory(memberId);
    }

    public UsablePointResponse getMyUsablePoint(Long memberId) {
        return memberPointService.getUsablePoint(memberId);
    }

    public List<MemberCouponListItemResponse> getMyCoupons(Long memberId) {
        return couponQueryService.findMemberCoupons(memberId).stream()
            .map(r -> MemberCouponListItemResponse.of(
                r.id(), r.couponId(), r.name(), r.description(),
                r.discountType(), r.discountAmount(), r.maxDiscountAmount(),
                r.minOrderAmount(), r.useStartAt(), r.useEndAt(),
                r.expiredAt(), r.isUsed(), r.usedAt()
            ))
            .toList();
    }

    public List<MemberCouponListItemResponse> getMyAvailableCoupons(Long memberId) {
        return couponQueryService.findAvailableMemberCoupons(memberId).stream()
            .map(r -> MemberCouponListItemResponse.of(
                r.id(), r.couponId(), r.name(), r.description(),
                r.discountType(), r.discountAmount(), r.maxDiscountAmount(),
                r.minOrderAmount(), r.useStartAt(), r.useEndAt(),
                r.expiredAt(), r.isUsed(), r.usedAt()
            ))
            .toList();
    }

    public PageResponse<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        return memberReviewService.getMyReviews(memberId, page, size);
    }

    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        return memberReviewService.getMyReviewCount(memberId);
    }

    public PageResponse<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
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
