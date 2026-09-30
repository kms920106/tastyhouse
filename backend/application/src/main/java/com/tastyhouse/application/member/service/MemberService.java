package com.tastyhouse.application.member.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.coupon.port.in.CouponQueryUseCase;
import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;
import com.tastyhouse.application.member.port.in.MemberCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberScreenUseCase;
import com.tastyhouse.application.member.port.in.MemberStatsQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;
import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.member.port.out.MyGradeResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

@Component
@WebApp
public class MemberService implements MemberScreenUseCase {

    private final MemberQueryUseCase memberQueryUseCase;
    private final MemberCommandUseCase memberCommandUseCase;
    private final MemberAuthService memberAuthService;
    private final MemberStatsQueryUseCase memberStatsQueryUseCase;
    private final MemberShopService memberShopService;
    private final MemberReviewService memberReviewService;
    private final CouponQueryUseCase couponQueryUseCase;
    private final MemberGradeService memberGradeService;

    public MemberService(
        MemberQueryUseCase memberQueryUseCase,
        MemberCommandUseCase memberCommandUseCase,
        MemberAuthService memberAuthService,
        MemberStatsQueryUseCase memberStatsQueryUseCase,
        MemberShopService memberShopService,
        MemberReviewService memberReviewService,
        CouponQueryUseCase couponQueryUseCase,
        MemberGradeService memberGradeService
    ) {
        this.memberQueryUseCase = memberQueryUseCase;
        this.memberCommandUseCase = memberCommandUseCase;
        this.memberAuthService = memberAuthService;
        this.memberStatsQueryUseCase = memberStatsQueryUseCase;
        this.memberShopService = memberShopService;
        this.memberReviewService = memberReviewService;
        this.couponQueryUseCase = couponQueryUseCase;
        this.memberGradeService = memberGradeService;
    }

    @Override
    public void updateMyProfile(MemberProfileUpdateCommand command) {
        memberCommandUseCase.updateProfile(command);
    }

    @Override
    public String verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        return memberAuthService.createPersonalInfoVerifyToken(memberId);
    }

    @Override
    public MemberPersonalInfoResult getPersonalInfo(Long memberId) {
        return memberQueryUseCase.getPersonalInfo(memberId);
    }

    @Override
    public void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken) {
        Long memberId = command.memberId();
        String phoneNumber = command.phoneNumber();
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, smsVerifyToken, phoneNumber);
        }
        memberCommandUseCase.updatePersonalInfo(command);
    }

    @Override
    public void updatePassword(MemberPasswordUpdateCommand command, String verifyToken) {
        memberAuthService.verifyPersonalInfoToken(command.memberId(), verifyToken);
        memberCommandUseCase.updatePassword(command);
    }

    @Override
    public void withdrawMember(MemberWithdrawCommand command, String bearerToken) {
        memberCommandUseCase.withdraw(command);
        memberAuthService.invalidateAccessToken(bearerToken);
    }

    @Override
    public boolean checkNicknameAvailability(String nickname) {
        return memberQueryUseCase.checkNicknameAvailability(nickname);
    }

    @Override
    public boolean checkPhoneAvailability(String phoneNumber) {
        return memberQueryUseCase.checkPhoneAvailability(phoneNumber);
    }

    @Override
    public MyGradeResult getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }

    @Override
    public List<MyCouponListItemResult> getMyCoupons(Long memberId) {
        return couponQueryUseCase.getMyCoupons(memberId);
    }

    @Override
    public List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId) {
        return couponQueryUseCase.getMyAvailableCoupons(memberId);
    }

    @Override
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return memberReviewService.getMyReviews(memberId, page, size);
    }

    @Override
    public long getMyReviewCount(Long memberId) {
        return memberReviewService.getMyReviewCount(memberId);
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return memberShopService.getMyBookmarkedShops(memberId, page, size);
    }

    @Override
    public MemberWithProfileImageResult getMemberBasicProfile(Long targetMemberId) {
        return memberQueryUseCase.getMemberProfile(targetMemberId);
    }

    @Override
    public MemberWithProfileImageResult getMyProfile(Long memberId) {
        return memberQueryUseCase.getMyProfile(memberId);
    }

    @Override
    public MemberStatsResult getMemberStats(Long memberId) {
        return memberStatsQueryUseCase.getMemberStats(memberId);
    }
}
