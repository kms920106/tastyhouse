package com.tastyhouse.webapi.member;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.core.entity.user.WithdrawalReason;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.member.response.MemberCouponListItemResponse;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.MyBookmarkedPlaceListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.OtherMemberProfileResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;
import com.tastyhouse.webapi.member.response.VerifyPasswordResponse;
import com.tastyhouse.webapi.member.service.MemberAccountService;
import com.tastyhouse.webapi.member.service.MemberAuthService;
import com.tastyhouse.webapi.member.service.MemberCouponService;
import com.tastyhouse.webapi.member.service.MemberFollowService;
import com.tastyhouse.webapi.member.service.MemberGradeService;
import com.tastyhouse.webapi.member.service.MemberPlaceService;
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
    private final MemberPlaceService memberPlaceService;
    private final MemberReviewService memberReviewService;
    private final MemberCouponService memberCouponService;
    private final MemberGradeService memberGradeService;

    public MemberProfileResponse getMyProfile(Long memberId) {
        return memberAccountService.getMemberProfile(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    public void updateMyProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        memberAccountService.updateMemberProfile(memberId, nickname, statusMessage, profileImageFileId);
    }

    public VerifyPasswordResponse verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        String verifyToken = memberAuthService.createPersonalInfoVerifyToken(memberId);
        return new VerifyPasswordResponse(verifyToken);
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
        return memberCouponService.getMemberCoupons(memberId);
    }

    public List<MemberCouponListItemResponse> getMyAvailableCoupons(Long memberId) {
        return memberCouponService.getAvailableMemberCoupons(memberId);
    }

    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, PageRequest pageRequest) {
        return memberReviewService.getMyReviews(memberId, pageRequest);
    }

    public PageResult<MyBookmarkedPlaceListItemResponse> getMyBookmarkedPlaces(Long memberId, PageRequest pageRequest) {
        return memberPlaceService.getMyBookmarkedPlaces(memberId, pageRequest);
    }

    public OtherMemberProfileResponse getOtherMemberProfile(Long targetMemberId, Long viewerMemberId) {
        return memberFollowService.getOtherMemberProfile(targetMemberId, viewerMemberId);
    }

    public MemberStatsResponse getMemberStats(Long memberId) {
        return memberFollowService.getMemberStats(memberId);
    }
}
