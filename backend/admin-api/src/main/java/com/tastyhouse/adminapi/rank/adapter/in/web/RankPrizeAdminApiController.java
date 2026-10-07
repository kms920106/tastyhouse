package com.tastyhouse.adminapi.rank.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.rank.port.in.RankPrizeCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeCreateUseCase;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteUseCase;
import com.tastyhouse.application.rank.port.in.RankPrizeManagementDetailQueryUseCase;
import com.tastyhouse.application.rank.port.in.RankPrizeManagementListQueryUseCase;
import com.tastyhouse.application.rank.port.in.RankPrizeUpdateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeUpdateUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankPrizeCreateRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankPrizeUpdateRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.response.RankPrizeDetailResponse;
import com.tastyhouse.adminapi.rank.adapter.in.web.response.RankPrizeListItemResponse;

@Tag(name = "Rank Prize Admin", description = "랭킹 경품 관리자 API")
@RestController
@RequestMapping("/api/ranks")
class RankPrizeAdminApiController {

    private final RankPrizeManagementListQueryUseCase rankPrizeManagementListQueryUseCase;
    private final RankPrizeCreateUseCase rankPrizeCreateUseCase;
    private final RankPrizeManagementDetailQueryUseCase rankPrizeManagementDetailQueryUseCase;
    private final RankPrizeUpdateUseCase rankPrizeUpdateUseCase;
    private final RankPrizeDeleteUseCase rankPrizeDeleteUseCase;

    public RankPrizeAdminApiController(
        RankPrizeManagementListQueryUseCase rankPrizeManagementListQueryUseCase,
        RankPrizeCreateUseCase rankPrizeCreateUseCase,
        RankPrizeManagementDetailQueryUseCase rankPrizeManagementDetailQueryUseCase,
        RankPrizeUpdateUseCase rankPrizeUpdateUseCase,
        RankPrizeDeleteUseCase rankPrizeDeleteUseCase
    ) {
        this.rankPrizeManagementListQueryUseCase = rankPrizeManagementListQueryUseCase;
        this.rankPrizeCreateUseCase = rankPrizeCreateUseCase;
        this.rankPrizeManagementDetailQueryUseCase = rankPrizeManagementDetailQueryUseCase;
        this.rankPrizeUpdateUseCase = rankPrizeUpdateUseCase;
        this.rankPrizeDeleteUseCase = rankPrizeDeleteUseCase;
    }

    @Operation(summary = "랭킹 경품 목록 조회", description = "해당 기간의 등수별 경품 목록을 조회합니다.")
    @GetMapping("/v1/periods/{id}/prizes")
    public ResponseEntity<ApiResponse<List<RankPrizeListItemResponse>>> getPrizes(@PathVariable Long id) {
        List<RankPrizeListItemResponse> prizes = rankPrizeManagementListQueryUseCase.getPrizesByPeriod(id).stream()
            .map(RankPrizeListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(prizes));
    }

    @Operation(summary = "랭킹 경품 등록", description = "해당 기간에 새로운 경품을 등록합니다.")
    @PostMapping("/v1/periods/{id}/prizes")
    public ResponseEntity<ApiResponse<Long>> createPrize(
        @PathVariable Long id,
        @Valid @RequestBody RankPrizeCreateRequest request
    ) {
        RankPrizeCreateCommand command = request.toCommand(id);
        Long prizeId = rankPrizeCreateUseCase.createPrize(command);
        return ResponseEntity.ok(ApiResponse.success(prizeId));
    }

    @Operation(summary = "랭킹 경품 상세 조회", description = "랭킹 경품 상세를 조회합니다.")
    @GetMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<RankPrizeDetailResponse>> getPrize(@PathVariable Long prizeId) {
        RankPrizeDetailResponse response = RankPrizeDetailResponse.from(rankPrizeManagementDetailQueryUseCase.getPrize(prizeId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "랭킹 경품 수정", description = "기존 랭킹 경품을 수정합니다.")
    @PutMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<Void>> updatePrize(
        @PathVariable Long prizeId,
        @Valid @RequestBody RankPrizeUpdateRequest request
    ) {
        RankPrizeUpdateCommand command = request.toCommand(prizeId);
        rankPrizeUpdateUseCase.updatePrize(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 경품 삭제", description = "기존 랭킹 경품을 삭제합니다.")
    @DeleteMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<Void>> deletePrize(@PathVariable Long prizeId) {
        RankPrizeDeleteCommand command = RankPrizeDeleteCommand.of(prizeId);
        rankPrizeDeleteUseCase.deletePrize(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
