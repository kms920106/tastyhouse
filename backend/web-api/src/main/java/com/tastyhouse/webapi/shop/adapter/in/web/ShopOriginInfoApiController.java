package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.shop.response.ShopOriginInfoResponse;
import com.tastyhouse.webapplication.shop.port.in.ShopOriginInfoQueryUseCase;

/**
 * 손님용 가게 원산지 표시 조회 API.
 *
 * <p><b>인증이 필요하지 않다.</b> 원산지는 법령이 요구하는 표시 정보라 로그인 없이 가게를 둘러보는
 * 손님도 봐야 하므로, 가게 정보·공지·주문안내와 같이 {@code PublicPaths}에 등록된다
 * ({@code /api/shops/v1/*&#47;origin}). 등록을 빠뜨리면 비로그인 손님에게 401이 나가면서 원산지 영역이
 * 비므로, 이 컨트롤러를 옮기거나 경로를 바꿀 때 그 목록을 함께 고친다.
 *
 * <p>점주 앱(ceo-api)에 같은 경로의 조회가 따로 있다. 앱이 다르므로 충돌하지 않으며, 응답 형태는
 * 의도적으로 다르다 — 점주는 {@code updatedAt}을 받고 미설정 시 빈 폼용 기본값을 받지만, 손님은 둘 다
 * 받지 않는다.
 */
@Tag(name = "Shop Origin Info", description = "가게 원산지 표시 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOriginInfoApiController {

    private final ShopOriginInfoQueryUseCase shopOriginInfoQueryService;

    public ShopOriginInfoApiController(ShopOriginInfoQueryUseCase shopOriginInfoQueryService) {
        this.shopOriginInfoQueryService = shopOriginInfoQueryService;
    }

    @Operation(summary = "원산지 조회",
        description = "가게의 원산지 표시 정보를 조회합니다. 미설정이면 data가 null이므로 화면은 원산지 "
            + "영역을 감춥니다.")
    @GetMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<ShopOriginInfoResponse>> getOriginInfo(@PathVariable Long id) {
        ShopOriginInfoResponse response = shopOriginInfoQueryService.getOriginInfo(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
