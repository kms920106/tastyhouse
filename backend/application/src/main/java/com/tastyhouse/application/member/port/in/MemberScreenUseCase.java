package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.member.port.out.MyGradeResult;

@WebApp
public interface MemberScreenUseCase {

    void updateMyProfile(MemberProfileUpdateCommand command);

    String verifyPasswordAndIssueToken(Long memberId, String password);

    MemberPersonalInfoResult getPersonalInfo(Long memberId);

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken);

    void updatePassword(MemberPasswordUpdateCommand command, String verifyToken);

    void withdrawMember(MemberWithdrawCommand command, String bearerToken);

    boolean checkNicknameAvailability(String nickname);

    boolean checkPhoneAvailability(String phoneNumber);

    MyGradeResult getMyGrade(Long memberId);

    List<MyCouponListItemResult> getMyCoupons(Long memberId);

    List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId);

    PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size);

    long getMyReviewCount(Long memberId);

    PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size);

    MemberWithProfileImageResult getMemberBasicProfile(Long targetMemberId);

    MemberWithProfileImageResult getMyProfile(Long memberId);

    MemberStatsResult getMemberStats(Long memberId);
}
