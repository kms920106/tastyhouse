package com.tastyhouse.webapi.member;

import com.tastyhouse.webapi.member.request.UpdatePasswordRequest;
import com.tastyhouse.webapi.member.request.UpdatePersonalInfoRequest;
import com.tastyhouse.webapi.member.request.UpdateProfileRequest;
import com.tastyhouse.webapi.member.request.VerifyPasswordRequest;
import com.tastyhouse.webapi.member.request.WithdrawMemberRequest;
import com.tastyhouse.webapi.member.response.MemberCouponListItemResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.VerifyPasswordResponse;
import com.tastyhouse.webapi.member.response.MyBookmarkedPlaceListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member Me", description = "내 정보 관리 API")
public class MemberMeApiController {

    private final MemberFacade memberFacade;

    @Operation(summary = "프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다. (닉네임, 상태메시지, 프로필 이미지)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PutMapping("/v1/me/profile")
    public ResponseEntity<CommonResponse<Void>> updateMyProfile(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        memberFacade.updateMyProfile(userDetails.getMemberId(), request.nickname(), request.statusMessage(), request.profileImageFileId());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "비밀번호 인증 (개인정보 수정 진입)", description = "개인정보 수정 화면 진입 전 현재 비밀번호를 검증합니다. 검증 성공 시 5분간 유효한 verifyToken을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공 - verifyToken 반환", content = @Content(schema = @Schema(implementation = VerifyPasswordResponse.class))),
        @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1/me/verify-password")
    public ResponseEntity<CommonResponse<VerifyPasswordResponse>> verifyPassword(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @RequestBody VerifyPasswordRequest request
    ) {
        VerifyPasswordResponse response = memberFacade.verifyPasswordAndIssueToken(userDetails.getMemberId(), request.password());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "개인정보 조회", description = "개인정보 수정 화면에 표시할 현재 개인정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PersonalInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/me/personal-info")
    public ResponseEntity<CommonResponse<PersonalInfoResponse>> getMyPersonalInfo(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getPersonalInfo(userDetails.getMemberId())));
    }

    @Operation(
        summary = "개인정보 수정",
        description = "개인정보를 수정합니다. " +
                      "비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다. " +
                      "휴대폰번호를 변경하는 경우 SMS 인증으로 발급받은 X-Phone-Verify-Token 헤더도 함께 필요합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패 또는 토큰 누락/만료)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/v1/me/personal-info")
    public ResponseEntity<CommonResponse<Void>> updateMyPersonalInfo(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @RequestHeader(value = "X-Phone-Verify-Token", required = false) String phoneVerifyToken,
        @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        memberFacade.updatePersonalInfo(
            userDetails.getMemberId(), verifyToken, phoneVerifyToken,
            request.fullName(), request.phoneNumber(), request.birthDate(), request.gender(),
            request.pushNotificationEnabled(), request.marketingInfoEnabled(), request.eventInfoEnabled()
        );
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "내 등급 조회", description = "로그인한 회원의 현재 등급, 다음 등급, 현재 리뷰 수, 다음 등급까지 필요한 리뷰 수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/grade")
    public ResponseEntity<CommonResponse<MyGradeResponse>> getMyGrade(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyGrade(userDetails.getMemberId())));
    }

    @Operation(summary = "보유 포인트 조회", description = "현재 로그인한 회원의 사용 가능한 포인트와 이번달 소멸 예정 포인트를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point")
    public ResponseEntity<CommonResponse<PointResponse>> getMyPoint(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyPoint(userDetails.getMemberId())));
    }

    @Operation(summary = "보유 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 모든 쿠폰을 조회합니다. (사용 여부 무관)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/coupons")
    public ResponseEntity<CommonResponse<List<MemberCouponListItemResponse>>> getMyCoupons(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyCoupons(userDetails.getMemberId())));
    }

    @Operation(summary = "사용 가능한 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 사용 가능한 쿠폰을 조회합니다. (미사용 + 유효기간 내)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/coupons/available")
    public ResponseEntity<CommonResponse<List<MemberCouponListItemResponse>>> getMyAvailableCoupons(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyAvailableCoupons(userDetails.getMemberId())));
    }

    @Operation(summary = "포인트 내역 조회", description = "사용 가능 포인트, 이번달 소멸 예정 포인트, 포인트 적립/사용 내역 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point/history")
    public ResponseEntity<CommonResponse<PointHistoryResponse>> getMyPointHistory(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyPointHistory(userDetails.getMemberId())));
    }

    @Operation(summary = "사용 가능 포인트 조회 (주문용)", description = "주문 시 사용 가능한 포인트를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point/usable")
    public ResponseEntity<CommonResponse<UsablePointResponse>> getMyUsablePoint(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyUsablePoint(userDetails.getMemberId())));
    }

    @Operation(summary = "내가 작성한 리뷰 개수 조회", description = "로그인한 회원이 작성한 리뷰 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/reviews/count")
    public ResponseEntity<CommonResponse<MyReviewCountResponse>> getMyReviewCount(
        @CurrentUser CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMyReviewCount(userDetails.getMemberId())));
    }

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/reviews")
    public ResponseEntity<CommonResponse<List<MyReviewListItemResponse>>> getMyReviews(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<MyReviewListItemResponse> pageResult = memberFacade.getMyReviews(userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(summary = "내가 즐겨찾기한 플레이스 목록 조회", description = "로그인한 회원이 북마크한 플레이스 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/bookmarks")
    public ResponseEntity<CommonResponse<List<MyBookmarkedPlaceListItemResponse>>> getMyBookmarkedPlaces(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<MyBookmarkedPlaceListItemResponse> pageResult = memberFacade.getMyBookmarkedPlaces(userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "비밀번호를 변경합니다. 비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패, 비밀번호 불일치, 토큰 만료)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/v1/me/password")
    public ResponseEntity<CommonResponse<Void>> updateMyPassword(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        memberFacade.updatePassword(userDetails.getMemberId(), verifyToken, request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "회원 탈퇴", description = "탈퇴 사유를 선택하여 회원 탈퇴를 처리합니다. 탈퇴 즉시 Access Token이 무효화됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (탈퇴 사유 미선택 등)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/v1/me")
    public ResponseEntity<CommonResponse<Void>> withdrawMember(
        @CurrentUser CustomUserDetails userDetails,
        @RequestHeader("Authorization") String bearerToken,
        @Valid @RequestBody WithdrawMemberRequest request
    ) {
        memberFacade.withdrawMember(userDetails.getMemberId(), request.reason(), request.reasonDetail(), bearerToken);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
