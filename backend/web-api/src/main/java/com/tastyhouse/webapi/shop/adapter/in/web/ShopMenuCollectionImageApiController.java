package com.tastyhouse.webapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopMenuCollectionImageResponse;

/**
 * 손님용 메뉴모음컷 조회 API — 가게를 열었을 때 가장 먼저, 가장 상단에서 보이는 이미지.
 *
 * <p><b>승인된 것만</b> 표시 순서대로 내려간다. 검수 대기·반려 건과 그 사유는 점주 화면(ceo-api)에만
 * 있고 여기 응답에는 필드 자체가 없다.
 *
 * <p>ceo-api의 점주용 목록 조회와 <b>URL 경로가 같다</b>. 두 앱은 서로 다른 호스트·포트로 서비스되고
 * 응답 계약이 달라(점주는 상태·반려 사유 포함) 각 모듈이 자기 버전을 소유하는 것이 맞다.
 *
 * <p>인증이 필요 없다 — 가게 상세를 여는 첫 화면이라 비로그인 손님도 봐야 한다. 공개 경로 등록은
 * {@code PublicPaths}가 담당한다.
 */
@Tag(name = "Shop Menu Collection Image", description = "메뉴모음컷 조회 API")
@RestController
@RequestMapping("/api/shops")
public class ShopMenuCollectionImageApiController {

    private final ShopMenuCollectionImageQueryUseCase shopMenuCollectionImageQueryService;

    public ShopMenuCollectionImageApiController(
        ShopMenuCollectionImageQueryUseCase shopMenuCollectionImageQueryService
    ) {
        this.shopMenuCollectionImageQueryService = shopMenuCollectionImageQueryService;
    }

    @Operation(summary = "메뉴모음컷 목록 조회",
        description = "가게의 메뉴모음컷을 표시 순서대로 조회합니다. 관리자 승인이 완료된 것만 내려갑니다.")
    @GetMapping("/v1/{id}/menu-collection-images")
    public ResponseEntity<ApiResponse<List<ShopMenuCollectionImageResponse>>> getMenuCollectionImages(
        @PathVariable Long id
    ) {
        List<ShopMenuCollectionImageResponse> response =
            shopMenuCollectionImageQueryService.getMenuCollectionImages(id).stream()
                .map(ShopMenuCollectionImageResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
