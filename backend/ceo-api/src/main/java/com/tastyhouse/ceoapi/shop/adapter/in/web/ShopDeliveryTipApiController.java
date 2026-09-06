package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryTipDistanceUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryTipHolidayUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryTipRegionsUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryTipSchedulesUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryTipTiersUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryTipSettingResponse;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceRemoveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipHolidayUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsRemoveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipSchedulesUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTiersUpdateCommand;

@Tag(name = "Ceo Shop Delivery Tip", description = "점주 가게 배달팁 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryTipApiController {
    private final ShopDeliveryTipQueryUseCase shopDeliveryTipQueryService;
    private final ShopDeliveryTipCommandUseCase shopDeliveryTipCommandUseCase;

    public ShopDeliveryTipApiController(ShopDeliveryTipQueryUseCase shopDeliveryTipQueryService, ShopDeliveryTipCommandUseCase shopDeliveryTipCommandUseCase) {
        this.shopDeliveryTipQueryService = shopDeliveryTipQueryService;
        this.shopDeliveryTipCommandUseCase = shopDeliveryTipCommandUseCase;
    }

    @Operation(summary = "내 가게 배달팁 통합 조회", description = "로그인한 점주가 소유한 가게의 구간별·거리별·지역별·시간별·공휴일 배달팁을 통합 조회합니다. 아직 설정하지 않은 가게도 빈 값으로 응답합니다.")
    @GetMapping("/v1/{id}/delivery-tips")
    public ResponseEntity<ApiResponse<ShopDeliveryTipSettingResponse>> getDeliveryTips(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryTipSettingResponse response =
            ShopDeliveryTipSettingResponse.from(shopDeliveryTipQueryService.getDeliveryTips(userDetails.getCeoId(), id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 구간별 배달팁 설정", description = "로그인한 점주가 소유한 가게의 구간별 기본 배달팁을 통째로 교체합니다(최대 3구간, 주문금액 오름차순·배달팁 내림차순).")
    @PutMapping("/v1/{id}/delivery-tips/tiers")
    public ResponseEntity<ApiResponse<Void>> updateTiers(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryTipTiersUpdateRequest request
    ) {
        ShopDeliveryTipTiersUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.updateTiers(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 거리별 추가 배달팁 설정", description = "로그인한 점주가 소유한 가게의 거리별 추가 배달팁을 설정합니다. 지역별 배달팁이 남아 있으면 설정할 수 없습니다.")
    @PutMapping("/v1/{id}/delivery-tips/distance")
    public ResponseEntity<ApiResponse<Void>> updateDistanceTip(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryTipDistanceUpdateRequest request
    ) {
        ShopDeliveryTipDistanceUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.updateDistanceTip(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 거리별 추가 배달팁 해제", description = "로그인한 점주가 소유한 가게의 거리별 추가 배달팁을 해제합니다.")
    @DeleteMapping("/v1/{id}/delivery-tips/distance")
    public ResponseEntity<ApiResponse<Void>> removeDistanceTip(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryTipDistanceRemoveCommand command = ShopDeliveryTipDistanceRemoveCommand.of(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.removeDistanceTip(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 지역별 추가 배달팁 설정", description = "로그인한 점주가 소유한 가게의 지역별 추가 배달팁을 통째로 교체합니다. 배달가능지역으로 등록된 행정동만 지정할 수 있습니다.")
    @PutMapping("/v1/{id}/delivery-tips/regions")
    public ResponseEntity<ApiResponse<Void>> updateRegionTips(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryTipRegionsUpdateRequest request
    ) {
        ShopDeliveryTipRegionsUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.updateRegionTips(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 지역별 추가 배달팁 전체 삭제", description = "로그인한 점주가 소유한 가게의 지역별 추가 배달팁을 전부 삭제합니다.")
    @DeleteMapping("/v1/{id}/delivery-tips/regions")
    public ResponseEntity<ApiResponse<Void>> removeRegionTips(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryTipRegionsRemoveCommand command = ShopDeliveryTipRegionsRemoveCommand.of(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.removeRegionTips(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 시간별 추가 배달팁 설정", description = "로그인한 점주가 소유한 가게의 시간별 추가 배달팁을 통째로 교체합니다. 같은 요일 구분 안에서 시간대가 겹칠 수 없습니다.")
    @PutMapping("/v1/{id}/delivery-tips/schedules")
    public ResponseEntity<ApiResponse<Void>> updateScheduleTips(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryTipSchedulesUpdateRequest request
    ) {
        ShopDeliveryTipSchedulesUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.updateScheduleTips(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 공휴일 추가 배달팁 설정", description = "로그인한 점주가 소유한 가게의 공휴일 추가 배달팁을 설정합니다. 0원을 보내면 설정이 삭제됩니다.")
    @PutMapping("/v1/{id}/delivery-tips/holiday")
    public ResponseEntity<ApiResponse<Void>> updateHolidayTip(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryTipHolidayUpdateRequest request
    ) {
        ShopDeliveryTipHolidayUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryTipCommandUseCase.updateHolidayTip(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
