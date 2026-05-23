package com.tastyhouse.webapi.follow;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.follow.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.response.IsFollowingResponse;
import com.tastyhouse.webapi.follow.response.MemberSearchListItemResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.tastyhouse.webapi.security.CurrentUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 API")
public class FollowApiController {

    private final FollowService followService;

    @Operation(summary = "팔로우", description = "특정 회원을 팔로우합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팔로우 성공"),
        @ApiResponse(responseCode = "400", description = "자기 자신 팔로우 불가 또는 이미 팔로우 중"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "대상 회원을 찾을 수 없음")
    })
    @PostMapping("/v1/{memberId}")
    public ResponseEntity<CommonResponse<Void>> follow(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "팔로우할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        followService.follow(userDetails.getMemberId(), memberId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "언팔로우", description = "특정 회원을 언팔로우합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "언팔로우 성공"),
        @ApiResponse(responseCode = "400", description = "팔로우 관계가 존재하지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/v1/{memberId}")
    public ResponseEntity<CommonResponse<Void>> unfollow(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "언팔로우할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        followService.unfollow(userDetails.getMemberId(), memberId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "팔로워 삭제", description = "나를 팔로우한 팔로워를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팔로워 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "팔로우 관계가 존재하지 않음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/v1/followers/{followerId}")
    public ResponseEntity<CommonResponse<Void>> removeFollower(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "삭제할 팔로워 회원 ID", example = "2") @PathVariable Long followerId
    ) {
        followService.removeFollower(userDetails.getMemberId(), followerId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "팔로우 여부 조회", description = "내가 특정 회원을 팔로우 중인지 여부를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = IsFollowingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/{memberId}/is-following")
    public ResponseEntity<CommonResponse<IsFollowingResponse>> isFollowing(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "팔로우 여부를 확인할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        boolean isFollowing = followService.isFollowing(userDetails.getMemberId(), memberId);
        return ResponseEntity.ok(CommonResponse.success(IsFollowingResponse.of(memberId, isFollowing)));
    }

    @Operation(summary = "팔로잉 목록 조회", description = "특정 회원의 팔로잉 목록을 페이징하여 조회합니다. 본인 조회 시 각 팔로잉 회원에 대한 내 팔로우 여부가 포함됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/{memberId}/following")
    public ResponseEntity<CommonResponse<List<FollowMemberListItemResponse>>> getFollowingList(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<FollowMemberListItemResponse> pageResult = followService.getFollowingList(memberId, userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(summary = "팔로워 목록 조회", description = "특정 회원의 팔로워 목록을 페이징하여 조회합니다. 각 팔로워에 대한 내 팔로우 여부가 포함됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/{memberId}/followers")
    public ResponseEntity<CommonResponse<List<FollowMemberListItemResponse>>> getFollowerList(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<FollowMemberListItemResponse> pageResult = followService.getFollowerList(memberId, userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(summary = "팔로잉 목록 조회 (비로그인)", description = "특정 회원의 팔로잉 목록을 페이징하여 조회합니다. 팔로우 여부는 항상 false로 응답합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/{memberId}/following/public")
    public ResponseEntity<CommonResponse<List<FollowMemberListItemResponse>>> getPublicFollowingList(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<FollowMemberListItemResponse> pageResult = followService.getPublicFollowingList(memberId, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(summary = "팔로워 목록 조회 (비로그인)", description = "특정 회원의 팔로워 목록을 페이징하여 조회합니다. 팔로우 여부는 항상 false로 응답합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/{memberId}/followers/public")
    public ResponseEntity<CommonResponse<List<FollowMemberListItemResponse>>> getPublicFollowerList(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<FollowMemberListItemResponse> pageResult = followService.getPublicFollowerList(memberId, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }

    @Operation(summary = "회원 검색 (닉네임)", description = "닉네임으로 회원을 검색합니다. 각 회원에 대한 내 팔로우 여부가 포함됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/search")
    public ResponseEntity<CommonResponse<List<MemberSearchListItemResponse>>> searchMembers(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "검색할 닉네임", example = "맛집") @RequestParam String nickname,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<MemberSearchListItemResponse> pageResult = followService.searchMembersByNickname(nickname, userDetails.getMemberId(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        ));
    }
}
