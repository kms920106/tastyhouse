package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.ceoapplication.shop.port.in.ShopChangeHistoryQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopChangeHistorySearchRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopChangeCategoryResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopChangeHistoryListItemResponse;

@Tag(name = "Ceo Shop Change History", description = "점주 가게 변경이력 조회 API")
@RestController
@RequestMapping("/api/shops")
public class ShopChangeHistoryApiController {

    private final ShopChangeHistoryQueryUseCase shopChangeHistoryQueryService;

    public ShopChangeHistoryApiController(ShopChangeHistoryQueryUseCase shopChangeHistoryQueryService) {
        this.shopChangeHistoryQueryService = shopChangeHistoryQueryService;
    }

    @Operation(
        summary = "내 가게 변경이력 조회",
        description = "로그인한 점주가 소유한 가게의 설정 변경이력을 최신순으로 조회합니다. 대분류·중분류·날짜로 필터할 수 있으며, 조회 가능 기간은 최근 6개월입니다."
    )
    @GetMapping("/v1/{id}/change-histories")
    public ResponseEntity<ApiResponse<List<ShopChangeHistoryListItemResponse>>> getChangeHistories(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopChangeHistorySearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ShopChangeHistoryResult> pageResult = shopChangeHistoryQueryService.getChangeHistories(
                userDetails.getCeoId(),
                id,
                request.category(),
                request.changeType(),
                request.changedDate(),
                pageRequest.page(),
                pageRequest.size()
            );
        PaginationResponse<ShopChangeHistoryListItemResponse> response =
            PaginationResponse.from(pageResult.map(ShopChangeHistoryListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            response.content(),
            response.page(),
            response.size(),
            response.totalElements()
        ));
    }

    @Operation(
        summary = "변경이력 분류 카탈로그 조회",
        description = "변경이력 필터 드롭다운을 채우기 위한 대분류·중분류 목록입니다. 가게에 종속되지 않는 정적 카탈로그입니다."
    )
    @GetMapping("/v1/change-history-types")
    public ResponseEntity<ApiResponse<List<ShopChangeCategoryResponse>>> getChangeHistoryTypes() {
        List<ShopChangeCategoryResponse> response = shopChangeHistoryQueryService.getChangeHistoryTypes().stream()
            .map(ShopChangeCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
