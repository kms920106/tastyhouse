package com.tastyhouse.webapi.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPasswordVerifyUseCase;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoQueryUseCase;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberVerifiedPasswordUpdateUseCase;
import com.tastyhouse.application.member.port.in.MemberVerifiedPersonalInfoUpdateUseCase;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawWithLogoutUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.adapter.in.web.request.UpdatePasswordRequest;
import com.tastyhouse.webapi.member.adapter.in.web.request.UpdatePersonalInfoRequest;
import com.tastyhouse.webapi.member.adapter.in.web.request.VerifyPasswordRequest;
import com.tastyhouse.webapi.member.adapter.in.web.request.WithdrawMemberRequest;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberVerifyPasswordResponse;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Me Account", description = "내 계정 관리 API")
class MemberMeAccountApiController {

    private final MemberPasswordVerifyUseCase memberPasswordVerifyUseCase;
    private final MemberPersonalInfoQueryUseCase memberPersonalInfoQueryUseCase;
    private final MemberVerifiedPersonalInfoUpdateUseCase memberVerifiedPersonalInfoUpdateUseCase;
    private final MemberVerifiedPasswordUpdateUseCase memberVerifiedPasswordUpdateUseCase;
    private final MemberWithdrawWithLogoutUseCase memberWithdrawWithLogoutUseCase;

    public MemberMeAccountApiController(
        MemberPasswordVerifyUseCase memberPasswordVerifyUseCase,
        MemberPersonalInfoQueryUseCase memberPersonalInfoQueryUseCase,
        MemberVerifiedPersonalInfoUpdateUseCase memberVerifiedPersonalInfoUpdateUseCase,
        MemberVerifiedPasswordUpdateUseCase memberVerifiedPasswordUpdateUseCase,
        MemberWithdrawWithLogoutUseCase memberWithdrawWithLogoutUseCase
    ) {
        this.memberPasswordVerifyUseCase = memberPasswordVerifyUseCase;
        this.memberPersonalInfoQueryUseCase = memberPersonalInfoQueryUseCase;
        this.memberVerifiedPersonalInfoUpdateUseCase = memberVerifiedPersonalInfoUpdateUseCase;
        this.memberVerifiedPasswordUpdateUseCase = memberVerifiedPasswordUpdateUseCase;
        this.memberWithdrawWithLogoutUseCase = memberWithdrawWithLogoutUseCase;
    }

    @Operation(summary = "비밀번호 인증 (개인정보 수정 진입)", description = "개인정보 수정 화면 진입 전 현재 비밀번호를 검증합니다. 검증 성공 시 5분간 유효한 verifyToken을 반환합니다.")
    @PostMapping("/v1/me/verify-password")
    public ResponseEntity<ApiResponse<MemberVerifyPasswordResponse>> verifyPassword(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @RequestBody VerifyPasswordRequest request
    ) {
        MemberVerifyPasswordResponse response =
            MemberVerifyPasswordResponse.from(memberPasswordVerifyUseCase.verifyPasswordAndIssueToken(userDetails.getMemberId(), request.password()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "개인정보 조회", description = "개인정보 수정 화면에 표시할 현재 개인정보를 조회합니다.")
    @GetMapping("/v1/me/personal-info")
    public ResponseEntity<ApiResponse<MemberPersonalInfoResponse>> getMyPersonalInfo(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(MemberPersonalInfoResponse.from(memberPersonalInfoQueryUseCase.getPersonalInfo(userDetails.getMemberId()))));
    }

    @Operation(
        summary = "개인정보 수정",
        description = "개인정보를 수정합니다. " +
                      "비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다. " +
                      "휴대폰번호를 변경하는 경우 SMS 인증으로 발급받은 X-Sms-Verify-Token 헤더도 함께 필요합니다."
    )
    @PutMapping("/v1/me/personal-info")
    public ResponseEntity<ApiResponse<Void>> updateMyPersonalInfo(
        @CurrentUser MemberUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @RequestHeader(value = "X-Sms-Verify-Token", required = false) String smsVerifyToken,
        @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        MemberPersonalInfoUpdateCommand command = request.toCommand(userDetails.getMemberId());
        memberVerifiedPersonalInfoUpdateUseCase.updatePersonalInfo(command, verifyToken, smsVerifyToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "비밀번호를 변경합니다. 비밀번호 인증으로 발급받은 X-Verify-Token 헤더가 필요합니다."
    )
    @PutMapping("/v1/me/password")
    public ResponseEntity<ApiResponse<Void>> updateMyPassword(
        @CurrentUser MemberUserDetails userDetails,
        @RequestHeader("X-Verify-Token") String verifyToken,
        @Valid @RequestBody UpdatePasswordRequest request
    ) {
        MemberPasswordUpdateCommand command = request.toCommand(userDetails.getMemberId());
        memberVerifiedPasswordUpdateUseCase.updatePassword(command, verifyToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 탈퇴", description = "탈퇴 사유를 선택하여 회원 탈퇴를 처리합니다. 탈퇴 즉시 Access Token이 무효화됩니다.")
    @DeleteMapping("/v1/me")
    public ResponseEntity<ApiResponse<Void>> withdrawMember(
        @CurrentUser MemberUserDetails userDetails,
        @RequestHeader("Authorization") String bearerToken,
        @Valid @RequestBody WithdrawMemberRequest request
    ) {
        MemberWithdrawCommand command = request.toCommand(userDetails.getMemberId());
        memberWithdrawWithLogoutUseCase.withdrawMember(command, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
