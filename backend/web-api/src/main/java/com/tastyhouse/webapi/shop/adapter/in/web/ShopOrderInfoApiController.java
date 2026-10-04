package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.shop.port.in.ShopOrderInfoQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.shop.adapter.in.web.request.ScheduledOrderSlotSearchRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.request.ShopDeliveryTipSearchRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ScheduledOrderSlotsResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopDeliveryTipResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopOrderMethodResponse;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop Order Info", description = "가게 주문 정보 API")
class ShopOrderInfoApiController {

    private final ShopOrderInfoQueryUseCase shopOrderInfoQueryUseCase;

    public ShopOrderInfoApiController(ShopOrderInfoQueryUseCase shopOrderInfoQueryUseCase) {
        this.shopOrderInfoQueryUseCase = shopOrderInfoQueryUseCase;
    }

    @Operation(summary = "배달팁 조회", description = "가게의 배달팁 설정과 하한/상한을 조회합니다. 로그인 회원이 배달 주소 ID와 주문금액을 함께 주면 확정 배달팁과 산출 근거를 반환합니다.")
    @GetMapping("/v1/{id}/delivery-tip")
    public ResponseEntity<ApiResponse<ShopDeliveryTipResponse>> getShopDeliveryTip(
        @PathVariable Long id,
        @Valid @ModelAttribute ShopDeliveryTipSearchRequest search,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ShopDeliveryTipResponse deliveryTip = ShopDeliveryTipResponse.from(
            shopOrderInfoQueryUseCase.getShopDeliveryTip(
                id,
                userDetails == null ? null : userDetails.getMemberId(),
                search.deliveryAddressId(),
                search.orderAmount(),
                search.orderMethod()
            )
        );
        return ResponseEntity.ok(ApiResponse.success(deliveryTip));
    }

    @Operation(
        summary = "예약 가능 수령시간 조회",
        description = "가게의 예약 가능한 수령시간 슬롯을 30분 단위로 조회합니다. 예약주문 미운영이거나 "
            + "예약 가능한 시간이 없으면 available=false와 빈 목록을 반환합니다."
    )
    @GetMapping("/v1/{id}/scheduled-order-slots")
    public ResponseEntity<ApiResponse<ScheduledOrderSlotsResponse>> getScheduledOrderSlots(
        @PathVariable Long id,
        @Valid @ModelAttribute ScheduledOrderSlotSearchRequest search
    ) {
        ScheduledOrderSlotsResponse slots = ScheduledOrderSlotsResponse.from(
            shopOrderInfoQueryUseCase.getScheduledOrderSlots(id, search.orderMethod())
        );
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @Operation(summary = "주문 수단 조회", description = "가게에서 주문 가능한 수단을 조회합니다. 테이블 오더, 예약, 포장 정보를 포함합니다.")
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<ShopOrderMethodResponse>> getShopOrderMethods(@PathVariable Long id) {
        ShopOrderMethodResponse orderMethods =
            ShopOrderMethodResponse.from(shopOrderInfoQueryUseCase.getShopOrderMethods(id));
        ApiResponse<ShopOrderMethodResponse> response = ApiResponse.success(orderMethods);
        return ResponseEntity.ok(response);
    }
}
