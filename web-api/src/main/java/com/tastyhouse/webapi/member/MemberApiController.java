package com.tastyhouse.webapi.member;

import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import com.tastyhouse.core.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 공개 조회 API")
public class MemberApiController {

    private final MemberFacade memberFacade;

    @Operation(summary = "회원 프로필 조회", description = "특정 회원의 프로필 정보(닉네임, 등급, 상태메시지, 프로필 이미지)만 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MemberProfileResponse.class))),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/{memberId}/profile")
    public ResponseEntity<CommonResponse<MemberProfileResponse>> getMemberBasicProfile(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMemberBasicProfile(memberId)));
    }

    @Operation(summary = "회원 통계 조회", description = "특정 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MemberStatsResponse.class))),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/{memberId}/stats")
    public ResponseEntity<CommonResponse<MemberStatsResponse>> getMemberStats(
        @Parameter(description = "조회할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(CommonResponse.success(memberFacade.getMemberStats(memberId)));
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
        return ResponseEntity.ok(CommonResponse.success(memberFacade.checkPhoneAvailability(phoneNumber)));
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
        return ResponseEntity.ok(CommonResponse.success(memberFacade.checkNicknameAvailability(nickname)));
    }
}
