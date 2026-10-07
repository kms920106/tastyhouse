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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourUpdateUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBreakTimeSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBusinessHourSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBreakTimeResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBusinessHourResponse;

@Tag(name = "Shop Business Hour Admin", description = "가게 영업시간 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopBusinessHourAdminApiController {

    private final ShopBusinessHourCreateUseCase shopBusinessHourCreateUseCase;
    private final ShopBusinessHourUpdateUseCase shopBusinessHourUpdateUseCase;
    private final ShopBusinessHourDeleteUseCase shopBusinessHourDeleteUseCase;
    private final ShopBreakTimeCreateUseCase shopBreakTimeCreateUseCase;
    private final ShopBreakTimeUpdateUseCase shopBreakTimeUpdateUseCase;
    private final ShopBreakTimeDeleteUseCase shopBreakTimeDeleteUseCase;
    private final ShopBusinessHourManagementQueryUseCase shopBusinessHourManagementQueryUseCase;
    private final ShopBreakTimeManagementQueryUseCase shopBreakTimeManagementQueryUseCase;

    public ShopBusinessHourAdminApiController(
        ShopBusinessHourCreateUseCase shopBusinessHourCreateUseCase,
        ShopBusinessHourUpdateUseCase shopBusinessHourUpdateUseCase,
        ShopBusinessHourDeleteUseCase shopBusinessHourDeleteUseCase,
        ShopBreakTimeCreateUseCase shopBreakTimeCreateUseCase,
        ShopBreakTimeUpdateUseCase shopBreakTimeUpdateUseCase,
        ShopBreakTimeDeleteUseCase shopBreakTimeDeleteUseCase,
        ShopBusinessHourManagementQueryUseCase shopBusinessHourManagementQueryUseCase,
        ShopBreakTimeManagementQueryUseCase shopBreakTimeManagementQueryUseCase
    ) {
        this.shopBusinessHourCreateUseCase = shopBusinessHourCreateUseCase;
        this.shopBusinessHourUpdateUseCase = shopBusinessHourUpdateUseCase;
        this.shopBusinessHourDeleteUseCase = shopBusinessHourDeleteUseCase;
        this.shopBreakTimeCreateUseCase = shopBreakTimeCreateUseCase;
        this.shopBreakTimeUpdateUseCase = shopBreakTimeUpdateUseCase;
        this.shopBreakTimeDeleteUseCase = shopBreakTimeDeleteUseCase;
        this.shopBusinessHourManagementQueryUseCase = shopBusinessHourManagementQueryUseCase;
        this.shopBreakTimeManagementQueryUseCase = shopBreakTimeManagementQueryUseCase;
    }

    @Operation(summary = "운영시간 목록 조회", description = "가게의 운영시간 목록을 조회합니다.")
    @GetMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<ShopBusinessHourResponse>>> getBusinessHours(@PathVariable Long id) {
        List<ShopBusinessHourResponse> response = shopBusinessHourManagementQueryUseCase.getBusinessHours(id).stream()
            .map(ShopBusinessHourResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "운영시간 등록", description = "가게에 운영시간을 등록합니다.")
    @PostMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<Long>> createBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourManagementCreateCommand command = request.toCreateCommand(userDetails.getPrincipalId(), id);
        Long businessHourId = shopBusinessHourCreateUseCase.createBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(businessHourId));
    }

    @Operation(summary = "운영시간 수정", description = "등록된 운영시간을 수정합니다.")
    @PutMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> updateBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long businessHourId,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourManagementUpdateCommand command = request.toUpdateCommand(userDetails.getPrincipalId(), businessHourId);
        shopBusinessHourUpdateUseCase.updateBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "운영시간 삭제", description = "등록된 운영시간을 삭제합니다.")
    @DeleteMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHour(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long businessHourId
    ) {
        ShopBusinessHourManagementDeleteCommand command = ShopBusinessHourManagementDeleteCommand.of(userDetails.getPrincipalId(), businessHourId);
        shopBusinessHourDeleteUseCase.deleteBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 목록 조회", description = "가게의 브레이크타임 목록을 조회합니다.")
    @GetMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<List<ShopBreakTimeResponse>>> getBreakTimes(@PathVariable Long id) {
        List<ShopBreakTimeResponse> response = shopBreakTimeManagementQueryUseCase.getBreakTimes(id).stream()
            .map(ShopBreakTimeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "브레이크타임 등록", description = "가게에 브레이크타임을 등록합니다.")
    @PostMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<Long>> createBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeManagementCreateCommand command = request.toCreateCommand(userDetails.getPrincipalId(), id);
        Long breakTimeId = shopBreakTimeCreateUseCase.createBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(breakTimeId));
    }

    @Operation(summary = "브레이크타임 수정", description = "등록된 브레이크타임을 수정합니다.")
    @PutMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> updateBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long breakTimeId,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeManagementUpdateCommand command = request.toUpdateCommand(userDetails.getPrincipalId(), breakTimeId);
        shopBreakTimeUpdateUseCase.updateBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 삭제", description = "등록된 브레이크타임을 삭제합니다.")
    @DeleteMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBreakTime(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long breakTimeId
    ) {
        ShopBreakTimeManagementDeleteCommand command = ShopBreakTimeManagementDeleteCommand.of(userDetails.getPrincipalId(), breakTimeId);
        shopBreakTimeDeleteUseCase.deleteBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
