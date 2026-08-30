package com.tastyhouse.webapplication.member.port.in;

import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.member.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapplication.member.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberProfileResponse;
import com.tastyhouse.webapplication.member.response.MemberStatsResponse;
import com.tastyhouse.webapplication.member.response.MemberVerifyPasswordResponse;
import com.tastyhouse.webapplication.member.response.MyCouponListItemResponse;
import com.tastyhouse.webapplication.member.response.MyGradeResponse;
import com.tastyhouse.webapplication.member.response.MyProfileResponse;
import com.tastyhouse.webapplication.member.response.MyReviewCountResponse;
import com.tastyhouse.webapplication.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapplication.member.response.ShopBookmarkListItemResponse;

/**
 * 내 정보 화면 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MemberService})을 알지 않는다. 이 포트가 없으면
 * {@code webAdaptersShouldNotDependOnApplicationServices}가 위반된다 — 파사드는 접미어가
 * {@code *CommandService}/{@code *QueryService}가 아니라 이름 기반 규칙의 그물을 빠져나가지만,
 * 패키지 기준 규칙에는 걸린다.
 *
 * <p><b>파사드를 삭제하지 않고 포트를 씌우는 근거</b>: 이 파사드는 {@code FollowService}처럼 단순
 * 위임만 하는 것이 아니라 "토큰 검증 → 변경"처럼 여러 협력자를 순서대로 엮는 화면 단위 흐름을 갖는다.
 * 삭제하면 그 조립 책임이 컨트롤러로 올라가 인바운드 어댑터가 application 흐름을 알게 되므로,
 * 흐름은 application에 남기고 경계만 인터페이스로 노출한다.
 */
public interface MemberScreenUseCase {

    void updateMyProfile(MemberProfileUpdateCommand command);

    MemberVerifyPasswordResponse verifyPasswordAndIssueToken(Long memberId, String password);

    MemberPersonalInfoResponse getPersonalInfo(Long memberId);

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken);

    void updatePassword(MemberPasswordUpdateCommand command, String verifyToken);

    void withdrawMember(MemberWithdrawCommand command, String bearerToken);

    MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname);

    MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber);

    MyGradeResponse getMyGrade(Long memberId);

    List<MyCouponListItemResponse> getMyCoupons(Long memberId);

    List<MyCouponListItemResponse> getMyAvailableCoupons(Long memberId);

    PaginationResponse<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size);

    MyReviewCountResponse getMyReviewCount(Long memberId);

    PaginationResponse<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size);

    MemberProfileResponse getMemberBasicProfile(Long targetMemberId);

    MyProfileResponse getMyProfile(Long memberId);

    MemberStatsResponse getMemberStats(Long memberId);
}
