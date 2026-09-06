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
