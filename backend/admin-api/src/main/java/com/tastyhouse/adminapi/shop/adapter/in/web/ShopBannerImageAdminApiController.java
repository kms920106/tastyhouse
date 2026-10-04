package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopBannerImageSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBannerImageItemResponse;

@Tag(name = "Shop Banner Image Admin", description = "가게 배너 이미지 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopBannerImageAdminApiController {

    private final ShopBannerImageCreateUseCase shopBannerImageCreateUseCase;
    private final ShopBannerImageDeleteUseCase shopBannerImageDeleteUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopBannerImageAdminApiController(
        ShopBannerImageCreateUseCase shopBannerImageCreateUseCase,
        ShopBannerImageDeleteUseCase shopBannerImageDeleteUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopBannerImageCreateUseCase = shopBannerImageCreateUseCase;
        this.shopBannerImageDeleteUseCase = shopBannerImageDeleteUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "배너 이미지 목록 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerImageItemResponse>>> getBannerImages(@PathVariable Long id) {
        List<ShopBannerImageItemResponse> response = shopQueryUseCase.getBannerImages(id).stream()
            .map(ShopBannerImageItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배너 이미지 등록", description = "가게에 배너 이미지를 등록합니다.")
    @PostMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<Long>> createBannerImage(
        @PathVariable Long id,
        @Valid @RequestBody ShopBannerImageSaveRequest request
    ) {
        ShopBannerImageCreateCommand command = request.toCommand(id);
        Long bannerImageId = shopBannerImageCreateUseCase.createBannerImage(command);
        return ResponseEntity.ok(ApiResponse.success(bannerImageId));
    }

    @Operation(summary = "배너 이미지 삭제", description = "등록된 배너 이미지를 삭제합니다.")
    @DeleteMapping("/v1/banners/{bannerImageId}")
    public ResponseEntity<ApiResponse<Void>> deleteBannerImage(@PathVariable Long bannerImageId) {
        ShopBannerImageDeleteCommand command = ShopBannerImageDeleteCommand.of(bannerImageId);
        shopBannerImageDeleteUseCase.deleteBannerImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
