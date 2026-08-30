package com.tastyhouse.webapi.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.member.port.in.MemberScreenUseCase;
import com.tastyhouse.webapi.member.adapter.in.web.request.NicknameAvailabilityRequest;
import com.tastyhouse.webapi.member.adapter.in.web.request.PhoneAvailabilityRequest;
import com.tastyhouse.webapplication.member.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberProfileResponse;
import com.tastyhouse.webapplication.member.response.MemberStatsResponse;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 공개 조회 API")
public class MemberApiController {

    private final MemberScreenUseCase memberService;

    public MemberApiController(MemberScreenUseCase memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원 프로필 조회", description = "특정 회원의 프로필 정보(닉네임, 등급, 상태메시지, 프로필 이미지)만 조회합니다.")
    @GetMapping("/v1/{id}/profile")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberBasicProfile(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberBasicProfile(id)));
    }

    @Operation(summary = "회원 통계 조회", description = "특정 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @GetMapping("/v1/{id}/stats")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getMemberStats(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberStats(id)));
    }

    @Operation(summary = "휴대폰번호 가입 가능 여부 확인", description = "입력한 휴대폰번호로 이미 가입된 활성 회원이 있는지 확인합니다. 인증번호 발송 전에 호출합니다. 인증 없이 호출 가능합니다.")
    @GetMapping("/v1/phone/availability")
    public ResponseEntity<ApiResponse<MemberPhoneAvailabilityResponse>> checkPhoneAvailability(
        @Valid @ModelAttribute PhoneAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.checkPhoneAvailability(request.phoneNumber())));
    }

    @Operation(summary = "닉네임 중복확인", description = "사용하려는 닉네임의 사용 가능 여부를 확인합니다. 인증 없이 호출 가능합니다.")
    @GetMapping("/v1/nickname/availability")
    public ResponseEntity<ApiResponse<MemberNicknameAvailabilityResponse>> checkNicknameAvailability(
        @Valid @ModelAttribute NicknameAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.checkNicknameAvailability(request.nickname())));
    }
}
