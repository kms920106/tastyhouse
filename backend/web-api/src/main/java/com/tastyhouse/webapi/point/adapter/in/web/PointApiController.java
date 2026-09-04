package com.tastyhouse.webapi.point.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapplication.point.port.in.PointQueryUseCase;
import com.tastyhouse.webapi.point.adapter.in.web.response.PointHistoryResponse;
import com.tastyhouse.webapi.point.adapter.in.web.response.PointResponse;
import com.tastyhouse.webapi.point.adapter.in.web.response.PointUsableResponse;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Point", description = "내 포인트 조회 API")
public class PointApiController {

    private final PointQueryUseCase pointQueryService;

    public PointApiController(PointQueryUseCase pointQueryService) {
        this.pointQueryService = pointQueryService;
    }

    @Operation(summary = "보유 포인트 조회", description = "현재 로그인한 회원의 사용 가능한 포인트와 이번달 소멸 예정 포인트를 조회합니다.")
    @GetMapping("/v1/me/point")
    public ResponseEntity<ApiResponse<PointResponse>> getMyPoint(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(PointResponse.from(pointQueryService.getMemberPoint(userDetails.getMemberId()))));
    }

    @Operation(summary = "포인트 내역 조회", description = "사용 가능 포인트, 이번달 소멸 예정 포인트, 포인트 적립/사용 내역 목록을 조회합니다.")
    @GetMapping("/v1/me/point/history")
    public ResponseEntity<ApiResponse<PointHistoryResponse>> getMyPointHistory(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(PointHistoryResponse.from(pointQueryService.getPointHistory(userDetails.getMemberId()))));
    }

    @Operation(summary = "사용 가능 포인트 조회 (주문용)", description = "주문 시 사용 가능한 포인트를 조회합니다.")
    @GetMapping("/v1/me/point/usable")
    public ResponseEntity<ApiResponse<PointUsableResponse>> getMyUsablePoint(
        @CurrentUser MemberUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(PointUsableResponse.of(pointQueryService.getUsablePoint(userDetails.getMemberId()))));
    }
}
