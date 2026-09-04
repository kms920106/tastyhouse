package com.tastyhouse.webapi.follow.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapplication.follow.port.in.FollowCancelCommand;
import com.tastyhouse.webapplication.follow.port.in.FollowCommandUseCase;
import com.tastyhouse.webapplication.follow.port.in.FollowCreateCommand;
import com.tastyhouse.webapplication.follow.port.in.FollowQueryUseCase;
import com.tastyhouse.webapplication.follow.port.in.FollowerRemoveCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.follow.adapter.in.web.request.FollowSearchRequest;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowIsFollowingResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberSearchListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follow", description = "팔로우 API")
public class FollowApiController {

    private final FollowCommandUseCase followCommandUseCase;
    private final FollowQueryUseCase followQueryUseCase;

    public FollowApiController(FollowCommandUseCase followCommandUseCase, FollowQueryUseCase followQueryUseCase) {
        this.followCommandUseCase = followCommandUseCase;
        this.followQueryUseCase = followQueryUseCase;
    }

    @Operation(summary = "팔로우", description = "특정 회원을 팔로우합니다. 생성된 팔로우 관계의 식별자(id)를 반환합니다.")
    @PostMapping("/v1/{memberId}")
    public ResponseEntity<ApiResponse<Long>> follow(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "팔로우할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        FollowCreateCommand command = FollowCreateCommand.of(userDetails.getMemberId(), memberId);
        Long followId = followCommandUseCase.follow(command);
        return ResponseEntity.ok(ApiResponse.success(followId));
    }

    @Operation(summary = "언팔로우", description = "특정 회원을 언팔로우합니다.")
    @DeleteMapping("/v1/{memberId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "언팔로우할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        FollowCancelCommand command = FollowCancelCommand.of(userDetails.getMemberId(), memberId);
        followCommandUseCase.unfollow(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "팔로워 삭제", description = "나를 팔로우한 팔로워를 삭제합니다.")
    @DeleteMapping("/v1/followers/{followerId}")
    public ResponseEntity<ApiResponse<Void>> removeFollower(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "삭제할 팔로워 회원 ID", example = "2") @PathVariable Long followerId
    ) {
        FollowerRemoveCommand command = FollowerRemoveCommand.of(userDetails.getMemberId(), followerId);
        followCommandUseCase.removeFollower(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "팔로우 여부 조회", description = "내가 특정 회원을 팔로우 중인지 여부를 조회합니다.")
    @GetMapping("/v1/{memberId}/is-following")
    public ResponseEntity<ApiResponse<FollowIsFollowingResponse>> isFollowing(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "팔로우 여부를 확인할 회원 ID", example = "2") @PathVariable Long memberId
    ) {
        boolean isFollowing = followQueryUseCase.isFollowing(userDetails.getMemberId(), memberId);
        return ResponseEntity.ok(ApiResponse.success(FollowIsFollowingResponse.of(memberId, isFollowing)));
    }

    @Operation(summary = "팔로잉 목록 조회", description = "특정 회원의 팔로잉 목록을 페이징하여 조회합니다. 본인 조회 시 각 팔로잉 회원에 대한 내 팔로우 여부가 포함됩니다.")
    @GetMapping("/v1/{memberId}/following")
    public ResponseEntity<ApiResponse<List<FollowMemberListItemResponse>>> getFollowingList(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<FollowMemberListItemResponse> pageResult = PaginationResponse.from(
            followQueryUseCase.getFollowingList(memberId, userDetails.getMemberId(), pageRequest.page(), pageRequest.size())
                .map(FollowMemberListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "팔로워 목록 조회", description = "특정 회원의 팔로워 목록을 페이징하여 조회합니다. 각 팔로워에 대한 내 팔로우 여부가 포함됩니다.")
    @GetMapping("/v1/{memberId}/followers")
    public ResponseEntity<ApiResponse<List<FollowMemberListItemResponse>>> getFollowerList(
        @CurrentUser MemberUserDetails userDetails,
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<FollowMemberListItemResponse> pageResult = PaginationResponse.from(
            followQueryUseCase.getFollowerList(memberId, userDetails.getMemberId(), pageRequest.page(), pageRequest.size())
                .map(FollowMemberListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "팔로잉 목록 조회 (비로그인)", description = "특정 회원의 팔로잉 목록을 페이징하여 조회합니다. 팔로우 여부는 항상 false로 응답합니다.")
    @GetMapping("/v1/{memberId}/following/public")
    public ResponseEntity<ApiResponse<List<FollowMemberListItemResponse>>> getPublicFollowingList(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<FollowMemberListItemResponse> pageResult = PaginationResponse.from(
            followQueryUseCase.getFollowingList(memberId, null, pageRequest.page(), pageRequest.size())
                .map(FollowMemberListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "팔로워 목록 조회 (비로그인)", description = "특정 회원의 팔로워 목록을 페이징하여 조회합니다. 팔로우 여부는 항상 false로 응답합니다.")
    @GetMapping("/v1/{memberId}/followers/public")
    public ResponseEntity<ApiResponse<List<FollowMemberListItemResponse>>> getPublicFollowerList(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<FollowMemberListItemResponse> pageResult = PaginationResponse.from(
            followQueryUseCase.getFollowerList(memberId, null, pageRequest.page(), pageRequest.size())
                .map(FollowMemberListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }

    @Operation(summary = "회원 검색 (닉네임)", description = "닉네임으로 회원을 검색합니다. 각 회원에 대한 내 팔로우 여부가 포함됩니다.")
    @GetMapping("/v1/search")
    public ResponseEntity<ApiResponse<List<FollowMemberSearchListItemResponse>>> searchMembers(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @ModelAttribute FollowSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<FollowMemberSearchListItemResponse> pageResult = PaginationResponse.from(
            followQueryUseCase.searchMembersByNickname(search.nickname(), userDetails.getMemberId(), pageRequest.page(), pageRequest.size())
                .map(FollowMemberSearchListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }
}
