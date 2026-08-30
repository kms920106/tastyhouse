package com.tastyhouse.adminapi.point.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.point.adapter.in.web.request.PointDeductRequest;
import com.tastyhouse.adminapi.point.adapter.in.web.request.PointEarnRequest;
import com.tastyhouse.adminapi.point.adapter.in.web.request.PointSearchRequest;
import com.tastyhouse.adminapplication.point.response.PointBalanceResponse;
import com.tastyhouse.adminapplication.point.response.PointHistoryResponse;
import com.tastyhouse.adminapplication.point.port.in.PointCommandUseCase;
import com.tastyhouse.adminapplication.point.port.in.PointDeductCommand;
import com.tastyhouse.adminapplication.point.port.in.PointEarnCommand;
import com.tastyhouse.adminapplication.point.port.in.PointQueryUseCase;

@Tag(name = "Point Admin", description = "포인트 관리자 API")
@RestController
@RequestMapping("/api/points")
public class PointApiController {

    private final PointCommandUseCase pointCommandUseCase;
    private final PointQueryUseCase pointQueryUseCase;

    public PointApiController(PointCommandUseCase pointCommandUseCase, PointQueryUseCase pointQueryUseCase) {
        this.pointCommandUseCase = pointCommandUseCase;
        this.pointQueryUseCase = pointQueryUseCase;
    }

    @Operation(summary = "회원 포인트 잔액 조회", description = "회원의 사용 가능 포인트와 이번 달 소멸 예정 포인트를 조회합니다.")
    @GetMapping("/v1/members/{memberId}")
    public ResponseEntity<ApiResponse<PointBalanceResponse>> getPointBalance(@PathVariable Long memberId) {
        PointBalanceResponse response = pointQueryUseCase.getPointBalance(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 포인트 이력 조회", description = "회원의 포인트 적립/사용/환불 이력을 최신순으로 페이징 조회합니다. type은 EARNED/USE/REFUND 중 하나이며 미지정 시 전체 조회합니다.")
    @GetMapping("/v1/members/{memberId}/histories")
    public ResponseEntity<ApiResponse<List<PointHistoryResponse>>> getPointHistories(
        @PathVariable Long memberId,
        @Valid @ModelAttribute PointSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<PointHistoryResponse> pageResponse = pointQueryUseCase.getPointHistories(memberId, search.type(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "포인트 수동 적립", description = "관리자가 사유를 입력하여 회원에게 포인트를 수동으로 적립합니다.")
    @PostMapping("/v1/members/{memberId}/earn")
    public ResponseEntity<ApiResponse<Void>> earnPoint(
        @PathVariable Long memberId,
        @Valid @RequestBody PointEarnRequest request
    ) {
        PointEarnCommand command = request.toCommand(memberId);
        pointCommandUseCase.earnPoint(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포인트 수동 차감", description = "관리자가 사유를 입력하여 회원의 포인트를 수동으로 차감합니다. 보유 포인트보다 많은 금액은 차감할 수 없습니다.")
    @PostMapping("/v1/members/{memberId}/deduct")
    public ResponseEntity<ApiResponse<Void>> deductPoint(
        @PathVariable Long memberId,
        @Valid @RequestBody PointDeductRequest request
    ) {
        PointDeductCommand command = request.toCommand(memberId);
        pointCommandUseCase.deductPoint(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
