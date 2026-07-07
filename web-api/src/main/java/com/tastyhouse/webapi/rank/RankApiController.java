package com.tastyhouse.webapi.rank;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.rank.request.RankSearchRequest;
import com.tastyhouse.webapi.rank.response.MemberRankListItemResponse;
import com.tastyhouse.webapi.rank.response.RankDurationResponse;
import com.tastyhouse.webapi.rank.response.RankPrizeListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
@Tag(name = "Rank", description = "랭킹 관리 API")
public class RankApiController {

    private final RankService rankService;

    @Operation(summary = "랭킹 기간 조회", description = "현재 진행중인 랭킹의 시작일자와 종료일자를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "진행중인 랭킹 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/v1/duration")
    public ResponseEntity<ApiResponse<RankDurationResponse>> getDuration() {
        return rankService.getDuration()
            .map(duration -> ResponseEntity.ok(ApiResponse.success(duration)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "랭킹 경품 목록 조회", description = "현재 진행중인 랭킹의 등수별 경품 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/v1/prizes")
    public ResponseEntity<ApiResponse<List<RankPrizeListItemResponse>>> getPrizes() {
        List<RankPrizeListItemResponse> prizes = rankService.getPrizes();
        return ResponseEntity.ok(ApiResponse.success(prizes));
    }

    @Operation(summary = "멤버 리뷰 랭킹 조회", description = "유저별 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/v1/members")
    public ResponseEntity<ApiResponse<List<MemberRankListItemResponse>>> getMemberRankList(
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        List<MemberRankListItemResponse> ranks = rankService.getMemberRankList(search.type(), search.limit());
        return ResponseEntity.ok(ApiResponse.success(ranks));
    }

    @Operation(summary = "내 리뷰 랭킹 조회", description = "현재 로그인한 유저의 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/v1/members/me")
    public ResponseEntity<ApiResponse<MemberRankListItemResponse>> getMyMemberRank(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        MemberRankListItemResponse myRank = rankService.getMyMemberRank(userDetails.getMemberId(), search.type());
        return ResponseEntity.ok(ApiResponse.success(myRank));
    }
}
