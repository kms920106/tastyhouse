package com.tastyhouse.ceoapi.shop.adapter.in.web;

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
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopScheduledOrderUpdateRequest;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderUpdateCommand;

@Tag(name = "Ceo Shop Scheduled Order", description = "점주 가게 예약주문 설정 API")
@RestController
@RequestMapping("/api/shops")
public class ShopScheduledOrderApiController {
    private final ShopScheduledOrderCommandUseCase shopScheduledOrderCommandUseCase;

    public ShopScheduledOrderApiController(ShopScheduledOrderCommandUseCase shopScheduledOrderCommandUseCase) {
        this.shopScheduledOrderCommandUseCase = shopScheduledOrderCommandUseCase;
    }

    @Operation(
        summary = "내 가게 예약주문 운영 여부 변경",
        description = "로그인한 점주가 소유한 가게의 예약주문 운영 여부를 변경합니다. 설정 단위는 가게 하나이며 "
            + "주문유형별로 나눌 수 없습니다. 켜면 고객이 배달·포장 주문의 수령시간을 30분 단위로 예약할 수 있고, "
            + "끄면 신규 예약만 차단되며 이미 접수된 예약주문은 그대로 유지됩니다."
    )
    @PutMapping("/v1/{id}/scheduled-order")
    public ResponseEntity<ApiResponse<Void>> updateScheduledOrder(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopScheduledOrderUpdateRequest request
    ) {
        ShopScheduledOrderUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopScheduledOrderCommandUseCase.updateScheduledOrder(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
