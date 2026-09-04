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
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopMinOrderAmountUpdateRequest;
import com.tastyhouse.ceoapplication.shop.port.in.ShopMinOrderAmountCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopMinOrderAmountUpdateCommand;

/**
 * 점주 가게 최소주문금액 관리 API.
 *
 * <p>조회 전용 엔드포인트를 따로 두지 않는다({@code ShopStatusApiController}가 GET/PUT 쌍인 것과 다른 점).
 * 최소주문금액은 가게 상세 조회({@code GET /api/shops/v1/{id}})의 {@code minOrderAmount} 필드로 이미
 * 내려가고, 점주 대시보드가 가게 정보를 한 덩어리로 받아 설정 행들을 렌더하므로 별도 조회가 왕복만 늘린다.
 */
@Tag(name = "Ceo Shop Min Order Amount", description = "점주 가게 최소주문금액 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopMinOrderAmountApiController {

    private final ShopMinOrderAmountCommandUseCase shopMinOrderAmountCommandUseCase;

    public ShopMinOrderAmountApiController(ShopMinOrderAmountCommandUseCase shopMinOrderAmountCommandUseCase) {
        this.shopMinOrderAmountCommandUseCase = shopMinOrderAmountCommandUseCase;
    }

    @Operation(
        summary = "내 가게 최소주문금액 변경",
        description = "로그인한 점주가 소유한 가게의 최소주문금액을 변경합니다. 0은 미설정(제한 없음)이며, "
            + "설정 시 5,000원~30,000원 범위여야 합니다. 이 금액은 배달 주문에만 적용되고 픽업(포장)에는 적용되지 않습니다."
    )
    @PutMapping("/v1/{id}/min-order-amount")
    public ResponseEntity<ApiResponse<Void>> updateMinOrderAmount(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopMinOrderAmountUpdateRequest request
    ) {
        ShopMinOrderAmountUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopMinOrderAmountCommandUseCase.updateMinOrderAmount(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
