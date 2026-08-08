package com.tastyhouse.ceoapi.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopScheduledOrderUpdateRequest;

/**
 * 점주 가게 예약주문 설정 API.
 *
 * <p>조회 전용 엔드포인트를 따로 두지 않는다({@code ShopMinOrderAmountApiController}와 같은 이유) —
 * 현재 값은 가게 상세 조회({@code GET /api/shops/v1/{id}})의 {@code scheduledOrderEnabled} 필드로 이미
 * 내려가고, 점주 대시보드가 가게 정보를 한 덩어리로 받아 설정 행들을 렌더하므로 별도 조회가 왕복만 늘린다.
 */
@Tag(name = "Ceo Shop Scheduled Order", description = "점주 가게 예약주문 설정 API")
@RestController
@RequestMapping("/api/shops")
public class ShopScheduledOrderApiController {

    private final ShopScheduledOrderCommandService shopScheduledOrderCommandService;

    public ShopScheduledOrderApiController(ShopScheduledOrderCommandService shopScheduledOrderCommandService) {
        this.shopScheduledOrderCommandService = shopScheduledOrderCommandService;
    }

    @Operation(
        summary = "내 가게 예약주문 운영 여부 변경",
        description = "로그인한 점주가 소유한 가게의 예약주문 운영 여부를 변경합니다. 설정 단위는 가게 하나이며 "
            + "주문유형별로 나눌 수 없습니다. 켜면 고객이 배달·포장 주문의 수령시간을 30분 단위로 예약할 수 있고, "
            + "끄면 신규 예약만 차단되며 이미 접수된 예약주문은 그대로 유지됩니다."
    )
    @PutMapping("/v1/{id}/scheduled-order")
    public ResponseEntity<ApiResponse<Void>> updateScheduledOrder(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopScheduledOrderUpdateRequest request
    ) {
        shopScheduledOrderCommandService.updateScheduledOrder(userDetails.getCeoId(), id, request.enabled());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
