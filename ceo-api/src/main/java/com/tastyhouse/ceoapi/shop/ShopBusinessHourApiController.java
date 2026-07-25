package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import com.tastyhouse.ceoapi.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopBreakTimeSaveRequest;
import com.tastyhouse.ceoapi.shop.request.ShopBusinessHourSaveRequest;
import com.tastyhouse.ceoapi.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.ceoapi.shop.response.ShopBusinessHourResponse;

@Tag(name = "Ceo Shop Business Hour", description = "점주 가게 운영시간·브레이크타임 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopBusinessHourApiController {

    private final ShopBusinessHourService shopBusinessHourService;

    @Operation(summary = "내 가게 운영시간 목록 조회", description = "로그인한 점주가 소유한 가게의 운영시간 목록을 조회합니다.")
    @GetMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<ShopBusinessHourResponse>>> getBusinessHours(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopBusinessHourResponse> response = shopBusinessHourService.getBusinessHours(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 운영시간 등록", description = "로그인한 점주가 소유한 가게에 운영시간을 등록합니다.")
    @PostMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<Long>> createBusinessHour(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        Long businessHourId = shopBusinessHourService.createBusinessHour(
            userDetails.getCeoId(), id, request.dayType(), request.openTime(), request.closeTime(), request.isClosed(), request.is24Hours()
        );
        return ResponseEntity.ok(ApiResponse.success(businessHourId));
    }

    @Operation(summary = "내 가게 운영시간 수정", description = "로그인한 점주가 소유한 가게의 운영시간을 수정합니다.")
    @PutMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> updateBusinessHour(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long businessHourId,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        shopBusinessHourService.updateBusinessHour(
            userDetails.getCeoId(), businessHourId, request.dayType(), request.openTime(), request.closeTime(), request.isClosed(), request.is24Hours()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 운영시간 삭제", description = "로그인한 점주가 소유한 가게의 운영시간을 삭제합니다.")
    @DeleteMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHour(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long businessHourId
    ) {
        shopBusinessHourService.deleteBusinessHour(userDetails.getCeoId(), businessHourId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 브레이크타임 목록 조회", description = "로그인한 점주가 소유한 가게의 브레이크타임 목록을 조회합니다.")
    @GetMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<List<ShopBreakTimeResponse>>> getBreakTimes(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopBreakTimeResponse> response = shopBusinessHourService.getBreakTimes(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 브레이크타임 등록", description = "로그인한 점주가 소유한 가게에 브레이크타임을 등록합니다.")
    @PostMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<Long>> createBreakTime(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        Long breakTimeId = shopBusinessHourService.createBreakTime(
            userDetails.getCeoId(), id, request.dayType(), request.startTime(), request.endTime()
        );
        return ResponseEntity.ok(ApiResponse.success(breakTimeId));
    }

    @Operation(summary = "내 가게 브레이크타임 수정", description = "로그인한 점주가 소유한 가게의 브레이크타임을 수정합니다.")
    @PutMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> updateBreakTime(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long breakTimeId,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        shopBusinessHourService.updateBreakTime(
            userDetails.getCeoId(), breakTimeId, request.dayType(), request.startTime(), request.endTime()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 브레이크타임 삭제", description = "로그인한 점주가 소유한 가게의 브레이크타임을 삭제합니다.")
    @DeleteMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBreakTime(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long breakTimeId
    ) {
        shopBusinessHourService.deleteBreakTime(userDetails.getCeoId(), breakTimeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
