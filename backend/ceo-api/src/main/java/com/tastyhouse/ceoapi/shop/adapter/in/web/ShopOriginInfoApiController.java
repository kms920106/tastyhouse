package com.tastyhouse.ceoapi.shop.adapter.in.web;

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

import com.tastyhouse.ceoapplication.shop.port.in.ShopOriginInfoQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopOriginInfoUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopOriginInfoResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopOriginInfoCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopOriginInfoUpdateCommand;

/**
 * 점주 가게 원산지 표시 관리 API.
 *
 * <p>원산지는 <b>메뉴 단위가 아니라 가게 단위</b>로 한 번 작성한다 — 표시 지침이 "모든 음식에 같으면
 * 일괄 표시"처럼 하나의 문장 안에서 표현되기 때문이다. 그래서 경로도 메뉴가 아니라 가게에 달린다.
 *
 * <p>손님 앱(web-api)에 같은 경로의 조회가 따로 있다. 앱이 다르므로 충돌하지 않으며, 응답 형태는
 * 의도적으로 다르다 — 점주는 {@code updatedAt}을 받고 손님은 받지 않으며, 미설정일 때 점주는 빈 폼용
 * 기본값을, 손님은 {@code null}을 받는다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Shop Origin Info", description = "점주 가게 원산지 표시 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOriginInfoApiController {

    private final ShopOriginInfoQueryUseCase shopOriginInfoQueryService;
    private final ShopOriginInfoCommandUseCase shopOriginInfoCommandUseCase;

    public ShopOriginInfoApiController(ShopOriginInfoQueryUseCase shopOriginInfoQueryService, ShopOriginInfoCommandUseCase shopOriginInfoCommandUseCase) {
        this.shopOriginInfoQueryService = shopOriginInfoQueryService;
        this.shopOriginInfoCommandUseCase = shopOriginInfoCommandUseCase;
    }

    @Operation(summary = "내 가게 원산지 조회",
        description = "로그인한 점주가 소유한 가게의 원산지 표시 정보를 조회합니다. 미설정이어도 data는 "
            + "null이 아니라 sourceType=DIRECT·content=null로 내려가므로 화면이 분기 없이 빈 폼을 그릴 수 있습니다.")
    @GetMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<ShopOriginInfoResponse>> getOriginInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopOriginInfoResponse response =
            shopOriginInfoQueryService.getOriginInfo(userDetails.getCeoId(), id)
                .map(ShopOriginInfoResponse::from)
                .orElseGet(ShopOriginInfoResponse::empty);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 원산지 등록/수정",
        description = "원산지 표시 정보를 전체 교체합니다. sourceType=DIRECT면 content가, FRANCHISE_URL이면 "
            + "url이 필수이며, 입력 방식이 바뀌면 서버가 반대편 필드를 null로 정리합니다.")
    @PutMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<Void>> updateOriginInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopOriginInfoUpdateRequest request
    ) {
        ShopOriginInfoUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopOriginInfoCommandUseCase.updateOriginInfo(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
