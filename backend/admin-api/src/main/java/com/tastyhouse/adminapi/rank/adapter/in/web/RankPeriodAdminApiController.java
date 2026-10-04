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

import com.tastyhouse.application.rank.port.in.RankCommandUseCase;
import com.tastyhouse.application.rank.port.in.RankManagementQueryUseCase;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodUpdateCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankPeriodCreateRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.request.RankPeriodUpdateRequest;
import com.tastyhouse.adminapi.rank.adapter.in.web.response.RankPeriodDetailResponse;
import com.tastyhouse.adminapi.rank.adapter.in.web.response.RankPeriodListItemResponse;

@Tag(name = "Rank Period Admin", description = "랭킹 기간 관리자 API")
@RestController
@RequestMapping("/api/ranks")
class RankPeriodAdminApiController {

    private final RankCommandUseCase rankCommandUseCase;
    private final RankManagementQueryUseCase rankQueryUseCase;

    public RankPeriodAdminApiController(RankCommandUseCase rankCommandUseCase, RankManagementQueryUseCase rankQueryUseCase) {
        this.rankCommandUseCase = rankCommandUseCase;
        this.rankQueryUseCase = rankQueryUseCase;
    }

    @Operation(summary = "랭킹 기간 목록 조회", description = "등록된 랭킹 기간 목록을 조회합니다.")
    @GetMapping("/v1/periods")
    public ResponseEntity<ApiResponse<List<RankPeriodListItemResponse>>> getPeriods() {
        List<RankPeriodListItemResponse> periods = rankQueryUseCase.getPeriods().stream()
            .map(RankPeriodListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(periods));
    }

    @Operation(summary = "랭킹 기간 등록", description = "새로운 랭킹 기간을 등록합니다.")
    @PostMapping("/v1/periods")
    public ResponseEntity<ApiResponse<Long>> createPeriod(@Valid @RequestBody RankPeriodCreateRequest request) {
        RankPeriodCreateCommand command = request.toCommand();
        Long id = rankCommandUseCase.createPeriod(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "랭킹 기간 상세 조회", description = "랭킹 기간 상세를 조회합니다.")
    @GetMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<RankPeriodDetailResponse>> getPeriod(@PathVariable Long id) {
        RankPeriodDetailResponse response = RankPeriodDetailResponse.from(rankQueryUseCase.getPeriod(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "랭킹 기간 수정", description = "기존 랭킹 기간을 수정합니다.")
    @PutMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePeriod(
        @PathVariable Long id,
        @Valid @RequestBody RankPeriodUpdateRequest request
    ) {
        RankPeriodUpdateCommand command = request.toCommand(id);
        rankCommandUseCase.updatePeriod(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 기간 삭제", description = "기존 랭킹 기간을 삭제합니다.")
    @DeleteMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable Long id) {
        RankPeriodDeleteCommand command = RankPeriodDeleteCommand.of(id);
        rankCommandUseCase.deletePeriod(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
