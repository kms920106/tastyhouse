package com.tastyhouse.ceoapi.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopOrderNoticeUpsertRequest;
import com.tastyhouse.ceoapi.shop.response.ShopOrderNoticeResponse;

/**
 * 점주 주문안내(메뉴판 최상단 안내 문구) API.
 *
 * <p>{@code ShopNoticeApiController}(사장님 공지)와 별개 컨트롤러다 — 공지는 여러 건을 등록해 그중
 * 1건만 노출하는 목록형 자원({@code /notices/{noticeId}})이고, 주문안내는 가게당 1건 단일 자원
 * ({@code /order-notice})이라 경로 형태와 메서드 구성이 다르다.
 *
 * <p>웹(손님) 앱에도 같은 경로 {@code GET /api/shops/v1/{shopId}/order-notice}가 있다. 앱이 달라
 * 충돌하지 않으며, 응답 형태는 의도적으로 다르다 — 손님은 게시중단 여부·사유를 받지 않는다.
 */
@Tag(name = "Ceo Shop Order Notice", description = "점주 주문안내 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderNoticeApiController {

    private final ShopOrderNoticeQueryService shopOrderNoticeQueryService;
    private final ShopOrderNoticeCommandService shopOrderNoticeCommandService;

    public ShopOrderNoticeApiController(
        ShopOrderNoticeQueryService shopOrderNoticeQueryService,
        ShopOrderNoticeCommandService shopOrderNoticeCommandService
    ) {
        this.shopOrderNoticeQueryService = shopOrderNoticeQueryService;
        this.shopOrderNoticeCommandService = shopOrderNoticeCommandService;
    }

    @Operation(summary = "주문안내 조회", description = "가게의 주문안내를 조회합니다. 미설정이면 content가 null이며, 관리자 게시중단 여부와 사유가 함께 내려갑니다.")
    @GetMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<ShopOrderNoticeResponse>> getOrderNotice(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopOrderNoticeResponse response = shopOrderNoticeQueryService.getOrderNotice(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문안내 등록·수정", description = "가게의 주문안내를 등록하거나 수정합니다(가게당 1건 전체교체). 승인 절차 없이 즉시 손님 화면에 반영됩니다.")
    @PutMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<Void>> upsertOrderNotice(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderNoticeUpsertRequest request
    ) {
        shopOrderNoticeCommandService.upsertOrderNotice(userDetails.getCeoId(), id, request.content());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
