package com.tastyhouse.adminapi.rank.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.rank.port.in.RankAggregateCommand;
import com.tastyhouse.application.rank.port.in.RankCommandUseCase;
import com.tastyhouse.application.rank.port.in.RankManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankAggregateRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankSearchRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.response.RankMemberListItemResponse;

@Tag(name = "Rank Admin", description = "랭킹 관리자 API")
@RestController
@RequestMapping("/api/ranks")
class RankApiController {

    private final RankCommandUseCase rankCommandUseCase;
    private final RankManagementQueryUseCase rankQueryUseCase;

    public RankApiController(RankCommandUseCase rankCommandUseCase, RankManagementQueryUseCase rankQueryUseCase) {
        this.rankCommandUseCase = rankCommandUseCase;
        this.rankQueryUseCase = rankQueryUseCase;
    }

    @Operation(summary = "회원 랭킹 목록 조회", description = "유저별 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @GetMapping("/v1/members")
    public ResponseEntity<ApiResponse<List<RankMemberListItemResponse>>> getMemberRankList(
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        List<RankMemberListItemResponse> ranks = rankQueryUseCase.getMemberRankList(search.type(), search.limit()).stream()
            .map(RankMemberListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(ranks));
    }

    @Operation(summary = "랭킹 수동 집계", description = "랭킹 집계를 수동으로 실행합니다. type 미지정 시 전체 타입(ALL/MONTHLY/WEEKLY) 재집계, type 지정 시 해당 타입만 baseDate 기준 재집계합니다.")
    @PostMapping("/v1/aggregations")
    public ResponseEntity<ApiResponse<Void>> aggregate(@Valid @RequestBody RankAggregateRequest request) {
        RankAggregateCommand command = request.toCommand();
        rankCommandUseCase.aggregate(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
