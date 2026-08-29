package com.tastyhouse.ceoapi.ceo.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.request.CeoLoginHistorySearchRequest;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.response.CeoLoginHistoryListItemResponse;
import com.tastyhouse.ceoapi.ceo.application.service.CeoLoginHistoryQueryService;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;

@Tag(name = "Ceo Login History", description = "점주 개인정보 접속기록(로그인 이력) 조회 API")
@RestController
@RequestMapping("/api/ceos")
public class CeoLoginHistoryApiController {

    private final CeoLoginHistoryQueryService ceoLoginHistoryQueryService;

    public CeoLoginHistoryApiController(CeoLoginHistoryQueryService ceoLoginHistoryQueryService) {
        this.ceoLoginHistoryQueryService = ceoLoginHistoryQueryService;
    }

    /**
     * 가게 식별자를 받지 않는다 — 로그인 이력은 계정 단위라 가게에 종속되지 않고, 인가는 토큰의
     * {@code ceoId}로만 필터하는 것 자체다.
     */
    @Operation(
        summary = "내 로그인 이력 조회",
        description = "로그인한 점주 본인의 개인정보처리시스템 접속기록(로그인 이력)을 최신순으로 조회합니다. 결과·기간으로 필터할 수 있으며, 조회 가능 기간은 최근 90일입니다."
    )
    @GetMapping("/v1/me/login-histories")
    public ResponseEntity<ApiResponse<List<CeoLoginHistoryListItemResponse>>> getLoginHistories(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute CeoLoginHistorySearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<CeoLoginHistoryListItemResponse> response =
            ceoLoginHistoryQueryService.getLoginHistories(
                userDetails.getCeoId(),
                request.result(),
                request.startDate(),
                request.endDate(),
                pageRequest.page(),
                pageRequest.size()
            );
        return ResponseEntity.ok(ApiResponse.success(
            response.content(),
            response.page(),
            response.size(),
            response.totalElements()
        ));
    }
}
