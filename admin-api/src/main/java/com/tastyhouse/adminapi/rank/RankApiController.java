package com.tastyhouse.adminapi.rank;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.rank.request.RankAggregateRequest;
import com.tastyhouse.adminapi.rank.request.RankPeriodCreateRequest;
import com.tastyhouse.adminapi.rank.request.RankPeriodUpdateRequest;
import com.tastyhouse.adminapi.rank.request.RankPrizeCreateRequest;
import com.tastyhouse.adminapi.rank.request.RankPrizeUpdateRequest;
import com.tastyhouse.adminapi.rank.request.RankSearchRequest;
import com.tastyhouse.adminapi.rank.response.RankMemberListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeListItemResponse;

@Tag(name = "Rank Admin", description = "랭킹 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ranks")
public class RankApiController {

    private final RankService rankService;

    @Operation(summary = "회원 랭킹 목록 조회", description = "유저별 리뷰 작성 개수 기준 랭킹을 조회합니다. (전체/월간/주간)")
    @GetMapping("/v1/members")
    public ResponseEntity<ApiResponse<List<RankMemberListItemResponse>>> getMemberRankList(
        @Valid @ModelAttribute RankSearchRequest search
    ) {
        List<RankMemberListItemResponse> ranks = rankService.getMemberRankList(search.type(), search.limit());
        return ResponseEntity.ok(ApiResponse.success(ranks));
    }

    @Operation(summary = "랭킹 수동 집계", description = "랭킹 집계를 수동으로 실행합니다. type 미지정 시 전체 타입(ALL/MONTHLY/WEEKLY) 재집계, type 지정 시 해당 타입만 baseDate 기준 재집계합니다.")
    @PostMapping("/v1/aggregations")
    public ResponseEntity<ApiResponse<Void>> aggregate(@Valid @RequestBody RankAggregateRequest request) {
        rankService.aggregate(request.type(), request.baseDate(), request.limit());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 기간 목록 조회", description = "등록된 랭킹 기간 목록을 조회합니다.")
    @GetMapping("/v1/periods")
    public ResponseEntity<ApiResponse<List<RankPeriodListItemResponse>>> getPeriods() {
        List<RankPeriodListItemResponse> periods = rankService.getPeriods();
        return ResponseEntity.ok(ApiResponse.success(periods));
    }

    @Operation(summary = "랭킹 기간 등록", description = "새로운 랭킹 기간을 등록합니다.")
    @PostMapping("/v1/periods")
    public ResponseEntity<ApiResponse<Long>> createPeriod(@Valid @RequestBody RankPeriodCreateRequest request) {
        Long id = rankService.createPeriod(request.startAt(), request.endAt(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "랭킹 기간 상세 조회", description = "랭킹 기간 상세를 조회합니다.")
    @GetMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<RankPeriodDetailResponse>> getPeriod(@PathVariable Long id) {
        RankPeriodDetailResponse response = rankService.getPeriod(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "랭킹 기간 수정", description = "기존 랭킹 기간을 수정합니다.")
    @PutMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePeriod(
        @PathVariable Long id,
        @Valid @RequestBody RankPeriodUpdateRequest request
    ) {
        rankService.updatePeriod(id, request.startAt(), request.endAt(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 기간 삭제", description = "기존 랭킹 기간을 삭제합니다.")
    @DeleteMapping("/v1/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable Long id) {
        rankService.deletePeriod(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 경품 목록 조회", description = "해당 기간의 등수별 경품 목록을 조회합니다.")
    @GetMapping("/v1/periods/{id}/prizes")
    public ResponseEntity<ApiResponse<List<RankPrizeListItemResponse>>> getPrizes(@PathVariable Long id) {
        List<RankPrizeListItemResponse> prizes = rankService.getPrizesByPeriod(id);
        return ResponseEntity.ok(ApiResponse.success(prizes));
    }

    @Operation(summary = "랭킹 경품 등록", description = "해당 기간에 새로운 경품을 등록합니다.")
    @PostMapping("/v1/periods/{id}/prizes")
    public ResponseEntity<ApiResponse<Long>> createPrize(
        @PathVariable Long id,
        @Valid @RequestBody RankPrizeCreateRequest request
    ) {
        Long prizeId = rankService.createPrize(id, request.prizeRank(), request.name(), request.brand(), request.imageFileId());
        return ResponseEntity.ok(ApiResponse.success(prizeId));
    }

    @Operation(summary = "랭킹 경품 상세 조회", description = "랭킹 경품 상세를 조회합니다.")
    @GetMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<RankPrizeDetailResponse>> getPrize(@PathVariable Long prizeId) {
        RankPrizeDetailResponse response = rankService.getPrize(prizeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "랭킹 경품 수정", description = "기존 랭킹 경품을 수정합니다.")
    @PutMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<Void>> updatePrize(
        @PathVariable Long prizeId,
        @Valid @RequestBody RankPrizeUpdateRequest request
    ) {
        rankService.updatePrize(prizeId, request.prizeRank(), request.name(), request.brand(), request.imageFileId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "랭킹 경품 삭제", description = "기존 랭킹 경품을 삭제합니다.")
    @DeleteMapping("/v1/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<Void>> deletePrize(@PathVariable Long prizeId) {
        rankService.deletePrize(prizeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
