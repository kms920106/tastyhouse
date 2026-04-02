package com.tastyhouse.webapi.rank;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.rank.response.MemberRankItem;
import com.tastyhouse.webapi.rank.response.MyRankResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
@Tag(name = "Rank", description = "랭킹 관리 API")
public class RankApiController {

    private final RankService rankService;

    @Operation(summary = "멤버 리뷰 랭킹 조회", description = "유저별 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/members")
    public ResponseEntity<CommonResponse<List<MemberRankItem>>> getMemberRankList(@Parameter(description = "랭킹 타입 (ALL, MONTHLY, WEEKLY)", example = "MONTHLY") @RequestParam(defaultValue = "ALL") String type, @Parameter(description = "조회할 랭킹 개수", example = "100") @RequestParam(defaultValue = "100") int limit) {
        List<MemberRankItem> ranks = rankService.getMemberRankList(type, limit);
        CommonResponse<List<MemberRankItem>> response = CommonResponse.success(ranks);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 리뷰 랭킹 조회", description = "현재 로그인한 유저의 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/members/me")
    public ResponseEntity<CommonResponse<MyRankResponse>> getMyMemberRank(@AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "랭킹 타입 (ALL, MONTHLY, WEEKLY)", example = "MONTHLY") @RequestParam(defaultValue = "ALL") String type) {
        MyRankResponse myRank = rankService.getMyMemberRank(userDetails.getMemberId(), type);
        CommonResponse<MyRankResponse> response = CommonResponse.success(myRank);
        return ResponseEntity.ok(response);
    }
}
