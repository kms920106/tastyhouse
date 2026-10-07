package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.application.shop.port.in.ShopClosedDayCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopClosedDaySaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopClosedDayResponse;

@Tag(name = "Shop Closed Day Admin", description = "가게 정기 휴무일 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopClosedDayAdminApiController {

    private final ShopClosedDayCreateUseCase shopClosedDayCreateUseCase;
    private final ShopClosedDayDeleteUseCase shopClosedDayDeleteUseCase;
    private final ShopClosedDayManagementQueryUseCase shopClosedDayManagementQueryUseCase;

    public ShopClosedDayAdminApiController(
        ShopClosedDayCreateUseCase shopClosedDayCreateUseCase,
        ShopClosedDayDeleteUseCase shopClosedDayDeleteUseCase,
        ShopClosedDayManagementQueryUseCase shopClosedDayManagementQueryUseCase
    ) {
        this.shopClosedDayCreateUseCase = shopClosedDayCreateUseCase;
        this.shopClosedDayDeleteUseCase = shopClosedDayDeleteUseCase;
        this.shopClosedDayManagementQueryUseCase = shopClosedDayManagementQueryUseCase;
    }

    @Operation(summary = "정기 휴무일 목록 조회", description = "가게의 정기 휴무일 목록을 조회합니다.")
    @GetMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<List<ShopClosedDayResponse>>> getClosedDays(@PathVariable Long id) {
        List<ShopClosedDayResponse> response = shopClosedDayManagementQueryUseCase.getClosedDays(id).stream()
            .map(ShopClosedDayResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "정기 휴무일 등록", description = "가게에 정기 휴무일을 등록합니다.")
    @PostMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<Long>> createClosedDay(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopClosedDaySaveRequest request
    ) {
        ShopClosedDayManagementCreateCommand command = request.toCommand(userDetails.getPrincipalId(), id);
        Long closedDayId = shopClosedDayCreateUseCase.createClosedDay(command);
        return ResponseEntity.ok(ApiResponse.success(closedDayId));
    }

    @Operation(summary = "정기 휴무일 삭제", description = "등록된 정기 휴무일을 삭제합니다.")
    @DeleteMapping("/v1/closed-days/{closedDayId}")
    public ResponseEntity<ApiResponse<Void>> deleteClosedDay(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long closedDayId
    ) {
        ShopClosedDayManagementDeleteCommand command = ShopClosedDayManagementDeleteCommand.of(userDetails.getPrincipalId(), closedDayId);
        shopClosedDayDeleteUseCase.deleteClosedDay(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
