package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopOrderNoticeResponse;

/**
 * 손님용 주문안내 조회 API.
 *
 * <p><b>인증이 필요하지 않다.</b> 주문안내는 로그인 없이 가게를 둘러보는 손님도 봐야 하는 메뉴판
 * 구성 요소이므로, 가게 정보·배너·공지와 같이 {@code PublicPaths}에 등록된다
 * ({@code /api/shops/v1/*&#47;order-notice}). 등록을 빠뜨리면 비로그인 손님에게 401이 나가면서
 * 메뉴판 최상단이 통째로 비므로, 이 컨트롤러를 옮기거나 경로를 바꿀 때 그 목록을 함께 고친다.
 *
 * <p>점주 앱(ceo-api)에 같은 경로의 조회가 따로 있다. 앱이 다르므로 충돌하지 않으며, 응답 형태는
 * 의도적으로 다르다 — 점주는 게시중단 여부·사유를 받고 손님은 받지 않는다.
 */
@Tag(name = "Shop Order Notice", description = "가게 주문안내 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderNoticeApiController {

    private final ShopOrderNoticeQueryUseCase shopOrderNoticeQueryService;

    public ShopOrderNoticeApiController(ShopOrderNoticeQueryUseCase shopOrderNoticeQueryService) {
        this.shopOrderNoticeQueryService = shopOrderNoticeQueryService;
    }

    @Operation(summary = "주문안내 조회", description = "가게의 주문안내를 조회합니다. 미설정이거나 관리자 게시중단 상태면 data가 null입니다.")
    @GetMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<ShopOrderNoticeResponse>> getOrderNotice(@PathVariable Long id) {
        ShopOrderNoticeResult result = shopOrderNoticeQueryService.getOrderNotice(id);
        ShopOrderNoticeResponse response = result == null ? null : ShopOrderNoticeResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
