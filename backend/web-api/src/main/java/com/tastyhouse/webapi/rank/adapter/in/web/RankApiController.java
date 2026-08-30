package com.tastyhouse.webapi.rank.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.auth.security.CustomUserDetails;
import com.tastyhouse.webapplication.rank.port.in.RankQueryUseCase;
import com.tastyhouse.webapi.rank.adapter.in.web.request.RankSearchRequest;
import com.tastyhouse.webapplication.rank.response.RankDurationResponse;
import com.tastyhouse.webapplication.rank.response.RankMemberListItemResponse;
import com.tastyhouse.webapplication.rank.response.RankPrizeListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/ranks")
@Tag(name = "Rank", description = "랭킹 관리 API")
public class RankApiController {

    private final RankQueryUseCase rankQueryService;

    public RankApiController(RankQueryUseCase rankQueryService) {
        this.rankQueryService = rankQueryService;
    }

    @Operation(summary = "랭킹 기간 조회", description = "현재 진행중인 랭킹의 시작일자와 종료일자를 조회합니다.")
    @GetMapping("/v1/duration")
    public ResponseEntity<ApiResponse<RankDurationResponse>> getDuration() {
        return rankQueryService.getDuration()
            .map(duration -> ResponseEntity.ok(ApiResponse.success(duration)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "랭킹 경품 목록 조회", description = "현재 진행중인 랭킹의 등수별 경품 목록을 조회합니다.")
    @GetMapping("/v1/prizes")
    public ResponseEntity<ApiResponse<List<RankPrizeListItemResponse>>> getPrizes() {
        List<RankPrizeListItemResponse> prizes = rankQueryService.getPrizes();
        return ResponseEntity.ok(ApiResponse.success(prizes));
    }

    @Operation(summary = "멤버 리뷰 랭킹 조회", description = "유저별 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @GetMapping("/v1/members")
    public ResponseEntity<ApiResponse<List<RankMemberListItemResponse>>> getMemberRankList(
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        List<RankMemberListItemResponse> ranks = rankQueryService.getMemberRankList(search.type(), search.limit());
        return ResponseEntity.ok(ApiResponse.success(ranks));
    }

    @Operation(summary = "내 리뷰 랭킹 조회", description = "현재 로그인한 유저의 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @GetMapping("/v1/members/me")
    public ResponseEntity<ApiResponse<RankMemberListItemResponse>> getMyMemberRank(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        RankMemberListItemResponse myRank = rankQueryService.getMyMemberRank(userDetails.getMemberId(), search.type());
        return ResponseEntity.ok(ApiResponse.success(myRank));
    }
}
