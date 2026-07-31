package com.tastyhouse.webapi.member;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.webapi.coupon.CouponQueryService;
import com.tastyhouse.webapi.member.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.MemberVerifyPasswordResponse;
import com.tastyhouse.webapi.member.response.MyCouponListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyProfileResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;
import com.tastyhouse.webapi.member.service.MemberAuthService;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.member.service.MemberGradeService;
import com.tastyhouse.webapi.member.service.MemberQueryService;
import com.tastyhouse.webapi.member.service.MemberReviewService;
import com.tastyhouse.webapi.member.service.MemberShopService;
import com.tastyhouse.webapi.member.service.MemberStatsQueryService;

/**
 * 내 정보 화면 컨트롤러 파사드.
 *
 * <p>회원 자체의 조회·변경은 CQRS 분리에 따라 {@link MemberQueryService}/{@link MemberCommandService}가
 * 담당하고, 이 클래스는 "토큰 검증 후 변경"처럼 여러 협력자를 순서대로 엮는 화면 단위 흐름과, 내 정보
 * 화면이 함께 보여주는 다른 컨텍스트(쿠폰·리뷰·북마크·등급·회원 통계) 위임만 얇게 유지한다.
 */
@Component
@RequiredArgsConstructor
public class MemberService {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final MemberAuthService memberAuthService;
    private final MemberStatsQueryService memberStatsQueryService;
    private final MemberShopService memberShopService;
    private final MemberReviewService memberReviewService;
    private final CouponQueryService couponQueryService;
    private final MemberGradeService memberGradeService;

    public void updateMyProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        memberCommandService.updateProfile(memberId, nickname, statusMessage, profileImageFileId);
    }

    public MemberVerifyPasswordResponse verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        String verifyToken = memberAuthService.createPersonalInfoVerifyToken(memberId);
        return MemberVerifyPasswordResponse.from(verifyToken);
    }

    public MemberPersonalInfoResponse getPersonalInfo(Long memberId) {
        return memberQueryService.getPersonalInfo(memberId);
    }

    public void updatePersonalInfo(Long memberId, String verifyToken,
                                   String smsVerifyToken, String fullName,
                                   String phoneNumber, Integer birthDate, String gender,
                                   boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                                   boolean eventInfoEnabled) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, smsVerifyToken, phoneNumber);
        }
        memberCommandService.updatePersonalInfo(memberId, fullName, phoneNumber, birthDate,
            gender == null ? null : MemberGender.from(gender),
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    public void updatePassword(Long memberId, String verifyToken, String newPassword, String newPasswordConfirm) {
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        memberAuthService.verifyNotSamePassword(memberId, newPassword);
        memberCommandService.updatePassword(memberId, newPassword, newPasswordConfirm);
    }

    public void withdrawMember(Long memberId, String reason, String reasonDetail, String bearerToken) {
        memberCommandService.withdraw(memberId, MemberWithdrawalReason.from(reason), reasonDetail);
        memberAuthService.invalidateAccessToken(bearerToken);
    }

    public MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        return memberQueryService.checkNicknameAvailability(nickname);
    }

    public MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        return memberQueryService.checkPhoneAvailability(phoneNumber);
    }

    public MyGradeResponse getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }

    public List<MyCouponListItemResponse> getMyCoupons(Long memberId) {
        return couponQueryService.getMyCoupons(memberId);
    }

    public List<MyCouponListItemResponse> getMyAvailableCoupons(Long memberId) {
        return couponQueryService.getMyAvailableCoupons(memberId);
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
        return memberQueryService.getMemberProfile(targetMemberId);
    }

    public MyProfileResponse getMyProfile(Long memberId) {
        return memberQueryService.getMyProfile(memberId);
    }

    public MemberStatsResponse getMemberStats(Long memberId) {
        return memberStatsQueryService.getMemberStats(memberId);
    }
}
