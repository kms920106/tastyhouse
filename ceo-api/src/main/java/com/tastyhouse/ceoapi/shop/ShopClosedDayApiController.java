package com.tastyhouse.ceoapi.shop;

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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopClosedDayCreateRequest;
import com.tastyhouse.ceoapi.shop.request.ShopHolidayClosureUpdateRequest;
import com.tastyhouse.ceoapi.shop.request.ShopTemporaryClosureCreateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopClosedDaysResponse;

@Tag(name = "Ceo Shop Closed Day", description = "점주 가게 휴무(공휴일·정기·임시) 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopClosedDayApiController {

    private final ShopClosedDayQueryService shopClosedDayQueryService;
    private final ShopClosedDayCommandService shopClosedDayCommandService;

    @Operation(summary = "내 가게 휴무 통합 조회", description = "로그인한 점주가 소유한 가게의 공휴일 휴무 여부·정기 휴무·임시 휴무를 통합 조회합니다.")
    @GetMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<ShopClosedDaysResponse>> getClosedDays(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopClosedDaysResponse response = shopClosedDayQueryService.getClosedDays(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 공휴일 휴무 설정", description = "로그인한 점주가 소유한 가게의 공휴일 휴무 여부를 변경합니다.")
    @PutMapping("/v1/{id}/closed-days/holiday")
    public ResponseEntity<ApiResponse<Void>> updateHolidayClosure(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopHolidayClosureUpdateRequest request
    ) {
        shopClosedDayCommandService.updateHolidayClosure(userDetails.getCeoId(), id, request.closedOnPublicHolidays());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 정기 휴무 추가", description = "로그인한 점주가 소유한 가게에 정기 휴무를 추가합니다(최대 15개).")
    @PostMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<Long>> createClosedDay(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopClosedDayCreateRequest request
    ) {
        Long closedDayId = shopClosedDayCommandService.createClosedDay(userDetails.getCeoId(), id, request.closedDayType());
        return ResponseEntity.ok(ApiResponse.success(closedDayId));
    }

    @Operation(summary = "내 가게 정기 휴무 삭제", description = "로그인한 점주가 소유한 가게의 정기 휴무를 삭제합니다.")
    @DeleteMapping("/v1/closed-days/{closedDayId}")
    public ResponseEntity<ApiResponse<Void>> deleteClosedDay(@PathVariable Long closedDayId) {
        shopClosedDayCommandService.deleteClosedDay(closedDayId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 임시 휴무 등록", description = "로그인한 점주가 소유한 가게에 임시 휴무를 등록합니다(누적 최대 30일).")
    @PostMapping("/v1/{id}/temporary-closures")
    public ResponseEntity<ApiResponse<Long>> createTemporaryClosure(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopTemporaryClosureCreateRequest request
    ) {
        Long temporaryClosureId = shopClosedDayCommandService.createTemporaryClosure(userDetails.getCeoId(), id, request.startDate(), request.endDate());
        return ResponseEntity.ok(ApiResponse.success(temporaryClosureId));
    }

    @Operation(summary = "내 가게 임시 휴무 삭제", description = "로그인한 점주가 소유한 가게의 임시 휴무를 삭제합니다.")
    @DeleteMapping("/v1/temporary-closures/{temporaryClosureId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemporaryClosure(@PathVariable Long temporaryClosureId) {
        shopClosedDayCommandService.deleteTemporaryClosure(temporaryClosureId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
