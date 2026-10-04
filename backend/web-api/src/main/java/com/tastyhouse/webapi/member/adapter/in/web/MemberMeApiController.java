package com.tastyhouse.webapi.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberScreenUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.adapter.in.web.request.UpdateProfileRequest;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyGradeResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyProfileResponse;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Me", description = "내 정보 관리 API")
class MemberMeApiController {

    private final MemberScreenUseCase memberUseCase;

    public MemberMeApiController(MemberScreenUseCase memberUseCase) {
        this.memberUseCase = memberUseCase;
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보(회원 ID, 닉네임, 등급, 상태메시지, 프로필 이미지)를 조회합니다.")
    @GetMapping("/v1/me/profile")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(MyProfileResponse.from(memberUseCase.getMyProfile(userDetails.getMemberId()))));
    }

    @Operation(summary = "프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다. (닉네임, 상태메시지, 프로필 이미지)")
    @PutMapping("/v1/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        MemberProfileUpdateCommand command = request.toCommand(userDetails.getMemberId());
        memberUseCase.updateMyProfile(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 통계 조회", description = "로그인한 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회합니다.")
    @GetMapping("/v1/me/stats")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getMyStats(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(MemberStatsResponse.from(memberUseCase.getMemberStats(userDetails.getMemberId()))));
    }

    @Operation(summary = "내 등급 조회", description = "로그인한 회원의 현재 등급, 다음 등급, 현재 리뷰 수, 다음 등급까지 필요한 리뷰 수를 조회합니다.")
    @GetMapping("/v1/me/grade")
    public ResponseEntity<ApiResponse<MyGradeResponse>> getMyGrade(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(MyGradeResponse.from(memberUseCase.getMyGrade(userDetails.getMemberId()))));
    }
}
