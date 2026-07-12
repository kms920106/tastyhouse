package com.tastyhouse.webapi.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.member.request.NicknameAvailabilityRequest;
import com.tastyhouse.webapi.member.request.PhoneAvailabilityRequest;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 공개 조회 API")
public class MemberApiController {

    private final MemberFacade memberFacade;

    @Operation(summary = "회원 프로필 조회", description = "특정 회원의 프로필 정보(닉네임, 등급, 상태메시지, 프로필 이미지)만 조회합니다.")
    @GetMapping("/v1/{memberId}/profile")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberBasicProfile(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberFacade.getMemberBasicProfile(memberId)));
    }

    @Operation(summary = "회원 통계 조회", description = "특정 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @GetMapping("/v1/{memberId}/stats")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getMemberStats(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberFacade.getMemberStats(memberId)));
    }

    @Operation(summary = "휴대폰번호 가입 가능 여부 확인", description = "입력한 휴대폰번호로 이미 가입된 활성 회원이 있는지 확인합니다. 인증번호 발송 전에 호출합니다. 인증 없이 호출 가능합니다.")
    @GetMapping("/v1/phone/availability")
    public ResponseEntity<ApiResponse<PhoneAvailabilityResponse>> checkPhoneAvailability(
        @Valid @ModelAttribute PhoneAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberFacade.checkPhoneAvailability(request.phoneNumber())));
    }

    @Operation(summary = "닉네임 중복확인", description = "사용하려는 닉네임의 사용 가능 여부를 확인합니다. 인증 없이 호출 가능합니다.")
    @GetMapping("/v1/nickname/availability")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
        @Valid @ModelAttribute NicknameAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberFacade.checkNicknameAvailability(request.nickname())));
    }
}
