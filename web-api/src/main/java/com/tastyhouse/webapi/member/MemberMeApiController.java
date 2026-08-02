package com.tastyhouse.webapi.member;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.request.UpdatePasswordRequest;
import com.tastyhouse.webapi.member.request.UpdatePersonalInfoRequest;
import com.tastyhouse.webapi.member.request.UpdateProfileRequest;
import com.tastyhouse.webapi.member.request.VerifyPasswordRequest;
import com.tastyhouse.webapi.member.request.WithdrawMemberRequest;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.MyCouponListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyProfileResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;
import com.tastyhouse.webapi.member.response.MemberVerifyPasswordResponse;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member Me", description = "내 정보 관리 API")
public class MemberMeApiController {

    private final MemberService memberService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보(회원 ID, 닉네임, 등급, 상태메시지, 프로필 이미지)를 조회합니다.")
    @GetMapping("/v1/me/profile")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyProfile(userDetails.getMemberId())));
    }

    @Operation(summary = "프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다. (닉네임, 상태메시지, 프로필 이미지)")
    @PutMapping("/v1/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        memberService.updateMyProfile(userDetails.getMemberId(), request.nickname(), request.statusMessage(), request.profileImageFileId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 통계 조회", description = "로그인한 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @GetMapping("/v1/me/stats")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getMyStats(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberStats(userDetails.getMemberId())));
    }

    @Operation(summary = "비밀번호 인증 (개인정보 수정 진입)", description = "개인정보 수정 화면 진입 전 현재 비밀번호를 검증합니다. 검증 성공 시 5분간 유효한 verifyToken을 반환합니다.")
    @PostMapping("/v1/me/verify-password")
    public ResponseEntity<ApiResponse<MemberVerifyPasswordResponse>> verifyPassword(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @RequestBody VerifyPasswordRequest request
    ) {
        MemberVerifyPasswordResponse response = memberService.verifyPasswordAndIssueToken(userDetails.getMemberId(), request.password());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "개인정보 조회", description = "개인정보 수정 화면에 표시할 현재 개인정보를 조회합니다.")
    @GetMapping("/v1/me/personal-info")
    public ResponseEntity<ApiResponse<MemberPersonalInfoResponse>> getMyPersonalInfo(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getPersonalInfo(userDetails.getMemberId())));
    }

    @Operation(
        summary = "개인정보 수정",
        description = "개인정보를 수정합니다. " +
                      "비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다. " +
                      "휴대폰번호를 변경하는 경우 SMS 인증으로 발급받은 X-Sms-Verify-Token 헤더도 함께 필요합니다."
    )
    @PutMapping("/v1/me/personal-info")
    public ResponseEntity<ApiResponse<Void>> updateMyPersonalInfo(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @RequestHeader(value = "X-Sms-Verify-Token", required = false) String smsVerifyToken,
        @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        memberService.updatePersonalInfo(
            userDetails.getMemberId(), verifyToken, smsVerifyToken,
            request.fullName(), request.phoneNumber(), request.birthDate(), request.gender(),
            request.pushNotificationEnabled(), request.marketingInfoEnabled(), request.eventInfoEnabled()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 등급 조회", description = "로그인한 회원의 현재 등급, 다음 등급, 현재 리뷰 수, 다음 등급까지 필요한 리뷰 수를 조회합니다.")
    @GetMapping("/v1/me/grade")
    public ResponseEntity<ApiResponse<MyGradeResponse>> getMyGrade(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyGrade(userDetails.getMemberId())));
    }

    @Operation(summary = "보유 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 모든 쿠폰을 조회합니다. (사용 여부 무관)")
    @GetMapping("/v1/me/coupons")
    public ResponseEntity<ApiResponse<List<MyCouponListItemResponse>>> getMyCoupons(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyCoupons(userDetails.getMemberId())));
    }

    @Operation(summary = "사용 가능한 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 사용 가능한 쿠폰을 조회합니다. (미사용 + 유효기간 내)")
    @GetMapping("/v1/me/coupons/available")
    public ResponseEntity<ApiResponse<List<MyCouponListItemResponse>>> getMyAvailableCoupons(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyAvailableCoupons(userDetails.getMemberId())));
    }

    @Operation(summary = "내가 작성한 리뷰 개수 조회", description = "로그인한 회원이 작성한 리뷰 개수를 조회합니다.")
    @GetMapping("/v1/me/reviews/count")
    public ResponseEntity<ApiResponse<MyReviewCountResponse>> getMyReviewCount(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyReviewCount(userDetails.getMemberId())));
    }

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/me/reviews")
    public ResponseEntity<ApiResponse<List<MyReviewListItemResponse>>> getMyReviews(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var pageResult = memberService.getMyReviews(userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "내가 즐겨찾기한 가게 목록 조회", description = "로그인한 회원이 북마크한 가게 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/me/bookmarks")
    public ResponseEntity<ApiResponse<List<ShopBookmarkListItemResponse>>> getMyBookmarkedShops(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var pageResult = memberService.getMyBookmarkedShops(userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "비밀번호를 변경합니다. 비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다."
    )
    @PutMapping("/v1/me/password")
    public ResponseEntity<ApiResponse<Void>> updateMyPassword(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        memberService.updatePassword(userDetails.getMemberId(), verifyToken, request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 탈퇴", description = "탈퇴 사유를 선택하여 회원 탈퇴를 처리합니다. 탈퇴 즉시 Access Token이 무효화됩니다.")
    @DeleteMapping("/v1/me")
    public ResponseEntity<ApiResponse<Void>> withdrawMember(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("Authorization") String bearerToken,
        @Valid @RequestBody WithdrawMemberRequest request
    ) {
        memberService.withdrawMember(userDetails.getMemberId(), request.reason(), request.reasonDetail(), bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
