package com.tastyhouse.webapi.member;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.coupon.response.MemberCouponListItemResponse;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.member.request.UpdatePasswordRequest;
import com.tastyhouse.webapi.member.request.UpdatePersonalInfoRequest;
import com.tastyhouse.webapi.member.request.UpdateProfileRequest;
import com.tastyhouse.webapi.member.request.VerifyPasswordRequest;
import com.tastyhouse.webapi.member.request.WithdrawMemberRequest;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.OtherMemberProfileResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.VerifyPasswordResponse;
import com.tastyhouse.webapi.member.response.MyBookmarkedPlaceListItemResponse;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Member", description = "회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Validated
public class MemberApiController {

    private final MemberService memberService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회합니다. (마이페이지용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MemberProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/me")
    public ResponseEntity<CommonResponse<MemberProfileResponse>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberProfileResponse profile = memberService.getMemberProfile(userDetails.getMemberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "회원을 찾을 수 없습니다."));
        return ResponseEntity.ok(CommonResponse.success(profile));
    }

    @Operation(summary = "프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다. (닉네임, 상태메시지, 프로필 이미지)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PutMapping("/v1/me/profile")
    public ResponseEntity<CommonResponse<Void>> updateMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        memberService.updateMemberProfile(
            userDetails.getMemberId(),
            request.nickname(),
            request.statusMessage(),
            request.profileImageFileId()
        );

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
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody VerifyPasswordRequest request
    ) {
        memberService.verifyPassword(userDetails.getMemberId(), request.password());

        String verifyToken = memberService.createPersonalInfoVerifyToken(userDetails.getMemberId());

        return ResponseEntity.ok(CommonResponse.success(
            new VerifyPasswordResponse(verifyToken)
        ));
    }

    @Operation(summary = "개인정보 조회", description = "개인정보 수정 화면에 표시할 현재 개인정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PersonalInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/me/personal-info")
    public ResponseEntity<CommonResponse<PersonalInfoResponse>> getMyPersonalInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PersonalInfoResponse response = memberService.getPersonalInfo(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(response));
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
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @RequestHeader(value = "X-Phone-Verify-Token", required = false) String phoneVerifyToken,
        @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        memberService.verifyPersonalInfoToken(userDetails.getMemberId(), verifyToken);

        String phoneNumberToUpdate = request.phoneNumber();
        if (phoneNumberToUpdate != null) {
            memberService.verifyPhoneToken(userDetails.getMemberId(), phoneVerifyToken, phoneNumberToUpdate);
        }

        memberService.updatePersonalInfo(
            userDetails.getMemberId(),
            request.fullName(),
            phoneNumberToUpdate,
            request.birthDate(),
            request.gender(),
            request.pushNotificationEnabled(),
            request.marketingInfoEnabled(),
            request.eventInfoEnabled()
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
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyGradeResponse myGrade = memberService.getMyGrade(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(myGrade));
    }

@Operation(summary = "보유 포인트 조회", description = "현재 로그인한 회원의 사용 가능한 포인트와 이번달 소멸 예정 포인트를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point")
    public ResponseEntity<CommonResponse<PointResponse>> getMyPoint(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointResponse pointResponse = memberService.getMemberPoint(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(pointResponse));
    }

    @Operation(summary = "보유 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 모든 쿠폰을 조회합니다. (사용 여부 무관)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/coupons")
    public ResponseEntity<CommonResponse<List<MemberCouponListItemResponse>>> getMyCoupons(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MemberCouponListItemResponse> coupons = memberService.getMemberCoupons(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(coupons));
    }

    @Operation(summary = "사용 가능한 쿠폰 목록 조회", description = "현재 로그인한 회원이 보유한 사용 가능한 쿠폰을 조회합니다. (미사용 + 유효기간 내)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/coupons/available")
    public ResponseEntity<CommonResponse<List<MemberCouponListItemResponse>>> getMyAvailableCoupons(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MemberCouponListItemResponse> coupons = memberService.getAvailableMemberCoupons(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(coupons));
    }

    @Operation(summary = "포인트 내역 조회", description = "사용 가능 포인트, 이번달 소멸 예정 포인트, 포인트 적립/사용 내역 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point/history")
    public ResponseEntity<CommonResponse<PointHistoryResponse>> getMyPointHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointHistoryResponse pointHistory = memberService.getPointHistory(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(pointHistory));
    }

    @Operation(summary = "사용 가능 포인트 조회 (주문용)", description = "주문 시 사용 가능한 포인트를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/point/usable")
    public ResponseEntity<CommonResponse<UsablePointResponse>> getMyUsablePoint(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UsablePointResponse usablePoint = memberService.getUsablePoint(userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(usablePoint));
    }

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/reviews")
    public ResponseEntity<CommonResponse<List<MyReviewListItemResponse>>> getMyReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "10") @Max(value = 10, message = "페이지 크기는 최대 10입니다") @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = new PageRequest(page, size);
        PageResult<MyReviewListItemResponse> pageResult = memberService.getMyReviews(userDetails.getMemberId(), pageRequest);
        CommonResponse<List<MyReviewListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 즐겨찾기한 플레이스 목록 조회", description = "로그인한 회원이 북마크한 플레이스 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/me/bookmarks")
    public ResponseEntity<CommonResponse<List<MyBookmarkedPlaceListItemResponse>>> getMyBookmarkedPlaces(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "10") @Max(value = 10, message = "페이지 크기는 최대 10입니다") @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = new PageRequest(page, size);
        PageResult<MyBookmarkedPlaceListItemResponse> pageResult = memberService.getMyBookmarkedPlaces(userDetails.getMemberId(), pageRequest);
        CommonResponse<List<MyBookmarkedPlaceListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
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
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        memberService.verifyPersonalInfoToken(userDetails.getMemberId(), verifyToken);
        memberService.updatePassword(userDetails.getMemberId(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "다른 회원 프로필 조회", description = "특정 회원의 프로필 정보(닉네임, 등급, 상태메시지, 프로필 이미지, 리뷰 수, 팔로잉/팔로워 수, 내 팔로우 여부)를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = OtherMemberProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/{memberId}/profile")
    public ResponseEntity<CommonResponse<OtherMemberProfileResponse>> getOtherMemberProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        OtherMemberProfileResponse profile = memberService.getOtherMemberProfile(memberId, userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(profile));
    }

    @Operation(summary = "특정 회원 통계 조회", description = "특정 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MemberStatsResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/{memberId}/stats")
    public ResponseEntity<CommonResponse<MemberStatsResponse>> getMemberStats(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        MemberStatsResponse stats = memberService.getMemberStats(memberId);
        return ResponseEntity.ok(CommonResponse.success(stats));
    }

    @Operation(summary = "회원 탈퇴", description = "탈퇴 사유를 선택하여 회원 탈퇴를 처리합니다. 탈퇴 즉시 Access Token이 무효화됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (탈퇴 사유 미선택 등)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/v1/me")
    public ResponseEntity<CommonResponse<Void>> withdrawMember(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody WithdrawMemberRequest request,
        @RequestHeader("Authorization") String bearerToken
    ) {
        memberService.withdrawMember(
            userDetails.getMemberId(),
            request.reason(),
            request.reasonDetail()
        );

        memberService.invalidateAccessToken(bearerToken);

        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "휴대폰번호 가입 가능 여부 확인", description = "입력한 휴대폰번호로 이미 가입된 활성 회원이 있는지 확인합니다. 인증번호 발송 전에 호출합니다. 인증 없이 호출 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 성공", content = @Content(schema = @Schema(implementation = PhoneAvailabilityResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (휴대폰번호 미입력)")
    })
    @GetMapping("/v1/phone/availability")
    public ResponseEntity<CommonResponse<PhoneAvailabilityResponse>> checkPhoneAvailability(
        @Parameter(description = "확인할 휴대폰번호", example = "01099841511")
        @NotBlank(message = "휴대폰번호를 입력해주세요.")
        @RequestParam String phoneNumber
    ) {
        PhoneAvailabilityResponse response = memberService.checkPhoneAvailability(phoneNumber);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "닉네임 중복확인", description = "사용하려는 닉네임의 사용 가능 여부를 확인합니다. 인증 없이 호출 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 성공", content = @Content(schema = @Schema(implementation = NicknameAvailabilityResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (닉네임 미입력 또는 길이 초과)")
    })
    @GetMapping("/v1/nickname/availability")
    public ResponseEntity<CommonResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
        @Parameter(description = "확인할 닉네임 (1~20자)", example = "맛있는탐험가")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하여야 합니다.")
        @RequestParam String nickname
    ) {
        NicknameAvailabilityResponse response = memberService.checkNicknameAvailability(nickname);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
