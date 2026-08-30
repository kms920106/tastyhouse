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
import com.tastyhouse.ceoapi.ceo.adapter.in.web.request.CeoShopAccessHistorySearchRequest;
import com.tastyhouse.ceoapplication.ceo.response.CeoShopAccessHistoryListItemResponse;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoShopAccessHistoryQueryUseCase;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;

@Tag(name = "Ceo Shop Access History", description = "점주 시스템 접근권한 부여·말소 이력 조회 API")
@RestController
@RequestMapping("/api/ceos")
public class CeoShopAccessHistoryApiController {

    private final CeoShopAccessHistoryQueryUseCase ceoShopAccessHistoryQueryService;

    public CeoShopAccessHistoryApiController(
        CeoShopAccessHistoryQueryUseCase ceoShopAccessHistoryQueryService
    ) {
        this.ceoShopAccessHistoryQueryService = ceoShopAccessHistoryQueryService;
    }

    /**
     * {@code shopId}는 필터일 뿐 인가 대상이 아니다 — 토큰의 {@code ceoId}로 함께 필터하므로 남의 가게
     * id를 넣으면 빈 목록이 되고, 가게 존재 여부가 새지 않는다.
     */
    @Operation(
        summary = "내 시스템 접근권한 이력 조회",
        description = "로그인한 점주 본인의 가게 접근권한 부여·말소 이력을 최신순으로 조회합니다. 조치 유형·가게·기간으로 필터할 수 있으며, 조회 가능 기간은 최근 5년입니다."
    )
    @GetMapping("/v1/me/shop-access-histories")
    public ResponseEntity<ApiResponse<List<CeoShopAccessHistoryListItemResponse>>> getShopAccessHistories(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute CeoShopAccessHistorySearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<CeoShopAccessHistoryListItemResponse> response =
            ceoShopAccessHistoryQueryService.getShopAccessHistories(
                userDetails.getCeoId(),
                request.actionType(),
                request.shopId(),
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
