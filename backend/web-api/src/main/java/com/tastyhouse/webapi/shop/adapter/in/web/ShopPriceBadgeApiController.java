package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.shop.port.in.ShopPriceBadgeQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopPriceBadgeResponse;

/**
 * 손님용 가게 매장가격 뱃지 조회 API.
 *
 * <p><b>인증이 필요하지 않다.</b> 뱃지는 로그인 없이 가게를 둘러보는 손님이 가격 신뢰도를 판단하는
 * 표시이므로, 가게 정보·공지·원산지와 같이 {@code PublicPaths}에 등록된다
 * ({@code /api/shops/v1/*&#47;price-badges}). 등록을 빠뜨리면 비로그인 손님에게 401이 나가면서 뱃지가
 * 사라지므로, 이 컨트롤러를 옮기거나 경로를 바꿀 때 그 목록을 함께 고친다.
 *
 * <p><b>메뉴 상세의 가격과 다른 층위다.</b> 메뉴마다 매장가를 내리지 않고 가게 단위 플래그 2개로만
 * 표현한다 — 매장가는 결제에 쓰이지 않는 표시 전용 값이라 손님 계약에 노출할 것이 아니고, 뱃지 조건
 * 자체가 가게의 전체 메뉴를 함께 봐야 성립한다(커버리지 80%).
 */
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
