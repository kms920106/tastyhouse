package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.shop.port.in.ShopPriceBadgeQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopPriceBadgeResponse;

@Tag(name = "Shop Price Badge", description = "가게 매장가격 뱃지 API")
@RestController
@RequestMapping("/api/shops")
public class ShopPriceBadgeApiController {
    private final ShopPriceBadgeQueryUseCase shopPriceBadgeQueryService;

    public ShopPriceBadgeApiController(ShopPriceBadgeQueryUseCase shopPriceBadgeQueryService) {
        this.shopPriceBadgeQueryService = shopPriceBadgeQueryService;
    }

    @Operation(summary = "매장가격 뱃지 조회",
        description = "가게에 노출할 매장가격 뱃지 2종의 노출 여부를 조회합니다. sameAsStorePrice는 가게의 "
            + "매장가격 인증이 켜져 있을 때, storePricePickup은 픽업가가 매장가 이하이고 전체 메뉴의 80% "
            + "이상이 매장가·픽업가를 가지며 픽업가 설정 익일(영업일)이 지났을 때 true입니다. 두 뱃지는 조건이 "
            + "달라 한쪽만 켜질 수 있습니다. 판정 근거(매장가 등)는 표시 전용 값이라 응답에 담지 않습니다.")
    @GetMapping("/v1/{id}/price-badges")
    public ResponseEntity<ApiResponse<ShopPriceBadgeResponse>> getPriceBadges(@PathVariable Long id) {
        ShopPriceBadgeResponse response =
            ShopPriceBadgeResponse.from(shopPriceBadgeQueryService.getPriceBadges(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
