package com.tastyhouse.ceoapi.shop.adapter.in.web;

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

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeListQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourListQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerUpdateUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopBreakTimeSaveRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopBusinessHourSaveRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopBreakTimeResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopBusinessHourResponse;

@Tag(name = "Ceo Shop Business Hour", description = "점주 가게 운영시간·브레이크타임 관리 API")
@RestController
@RequestMapping("/api/shops")
class ShopBusinessHourApiController {

    private final ShopBusinessHourListQueryUseCase shopBusinessHourListQueryUseCase;
    private final ShopBusinessHourOwnerCreateUseCase shopBusinessHourOwnerCreateUseCase;
    private final ShopBusinessHourOwnerUpdateUseCase shopBusinessHourOwnerUpdateUseCase;
    private final ShopBusinessHourOwnerDeleteUseCase shopBusinessHourOwnerDeleteUseCase;
    private final ShopBreakTimeListQueryUseCase shopBreakTimeListQueryUseCase;
    private final ShopBreakTimeOwnerCreateUseCase shopBreakTimeOwnerCreateUseCase;
    private final ShopBreakTimeOwnerUpdateUseCase shopBreakTimeOwnerUpdateUseCase;
    private final ShopBreakTimeOwnerDeleteUseCase shopBreakTimeOwnerDeleteUseCase;

    public ShopBusinessHourApiController(
        ShopBusinessHourListQueryUseCase shopBusinessHourListQueryUseCase,
        ShopBusinessHourOwnerCreateUseCase shopBusinessHourOwnerCreateUseCase,
        ShopBusinessHourOwnerUpdateUseCase shopBusinessHourOwnerUpdateUseCase,
        ShopBusinessHourOwnerDeleteUseCase shopBusinessHourOwnerDeleteUseCase,
        ShopBreakTimeListQueryUseCase shopBreakTimeListQueryUseCase,
        ShopBreakTimeOwnerCreateUseCase shopBreakTimeOwnerCreateUseCase,
        ShopBreakTimeOwnerUpdateUseCase shopBreakTimeOwnerUpdateUseCase,
        ShopBreakTimeOwnerDeleteUseCase shopBreakTimeOwnerDeleteUseCase
    ) {
        this.shopBusinessHourListQueryUseCase = shopBusinessHourListQueryUseCase;
        this.shopBusinessHourOwnerCreateUseCase = shopBusinessHourOwnerCreateUseCase;
        this.shopBusinessHourOwnerUpdateUseCase = shopBusinessHourOwnerUpdateUseCase;
        this.shopBusinessHourOwnerDeleteUseCase = shopBusinessHourOwnerDeleteUseCase;
        this.shopBreakTimeListQueryUseCase = shopBreakTimeListQueryUseCase;
        this.shopBreakTimeOwnerCreateUseCase = shopBreakTimeOwnerCreateUseCase;
        this.shopBreakTimeOwnerUpdateUseCase = shopBreakTimeOwnerUpdateUseCase;
        this.shopBreakTimeOwnerDeleteUseCase = shopBreakTimeOwnerDeleteUseCase;
    }

    @Operation(summary = "내 가게 운영시간 목록 조회", description = "로그인한 점주가 소유한 가게의 운영시간 목록을 조회합니다.")
    @GetMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<ShopBusinessHourResponse>>> getBusinessHours(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopBusinessHourResponse> response = shopBusinessHourListQueryUseCase.getBusinessHours(userDetails.getCeoId(), id).stream()
            .map(ShopBusinessHourResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 운영시간 등록", description = "로그인한 점주가 소유한 가게에 운영시간을 등록합니다.")
    @PostMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<Long>> createBusinessHour(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourOwnerCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long businessHourId = shopBusinessHourOwnerCreateUseCase.createBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(businessHourId));
    }

    @Operation(summary = "내 가게 운영시간 수정", description = "로그인한 점주가 소유한 가게의 운영시간을 수정합니다.")
    @PutMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> updateBusinessHour(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long businessHourId,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        ShopBusinessHourOwnerUpdateCommand command = request.toUpdateCommand(userDetails.getCeoId(), businessHourId);
        shopBusinessHourOwnerUpdateUseCase.updateBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 운영시간 삭제", description = "로그인한 점주가 소유한 가게의 운영시간을 삭제합니다.")
    @DeleteMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHour(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long businessHourId
    ) {
        ShopBusinessHourOwnerDeleteCommand command = ShopBusinessHourOwnerDeleteCommand.of(userDetails.getCeoId(), businessHourId);
        shopBusinessHourOwnerDeleteUseCase.deleteBusinessHour(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 브레이크타임 목록 조회", description = "로그인한 점주가 소유한 가게의 브레이크타임 목록을 조회합니다.")
    @GetMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<List<ShopBreakTimeResponse>>> getBreakTimes(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopBreakTimeResponse> response = shopBreakTimeListQueryUseCase.getBreakTimes(userDetails.getCeoId(), id).stream()
            .map(ShopBreakTimeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 브레이크타임 등록", description = "로그인한 점주가 소유한 가게에 브레이크타임을 등록합니다.")
    @PostMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<Long>> createBreakTime(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeOwnerCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long breakTimeId = shopBreakTimeOwnerCreateUseCase.createBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(breakTimeId));
    }

    @Operation(summary = "내 가게 브레이크타임 수정", description = "로그인한 점주가 소유한 가게의 브레이크타임을 수정합니다.")
    @PutMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> updateBreakTime(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long breakTimeId,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        ShopBreakTimeOwnerUpdateCommand command = request.toUpdateCommand(userDetails.getCeoId(), breakTimeId);
        shopBreakTimeOwnerUpdateUseCase.updateBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 브레이크타임 삭제", description = "로그인한 점주가 소유한 가게의 브레이크타임을 삭제합니다.")
    @DeleteMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBreakTime(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long breakTimeId
    ) {
        ShopBreakTimeOwnerDeleteCommand command = ShopBreakTimeOwnerDeleteCommand.of(userDetails.getCeoId(), breakTimeId);
        shopBreakTimeOwnerDeleteUseCase.deleteBreakTime(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
