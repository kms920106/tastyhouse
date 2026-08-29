package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopTrademarkQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopImageStatusResponse;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopThumbnailChangeRequestCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopTrademarkChangeRequestCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopTrademarkCommandUseCase;

@Tag(name = "Ceo Shop Trademark", description = "점주 가게 상표/대표이미지 변경요청 API")
@RestController
@RequestMapping("/api/shops")
public class ShopTrademarkApiController {

    private final ShopTrademarkQueryUseCase shopTrademarkQueryService;
    private final ShopTrademarkCommandUseCase shopTrademarkCommandUseCase;

    public ShopTrademarkApiController(ShopTrademarkQueryUseCase shopTrademarkQueryService, ShopTrademarkCommandUseCase shopTrademarkCommandUseCase) {
        this.shopTrademarkQueryService = shopTrademarkQueryService;
        this.shopTrademarkCommandUseCase = shopTrademarkCommandUseCase;
    }

    @Operation(summary = "상표 이미지 현황 조회", description = "가게의 현재 상표 이미지와 변경 요청 상태 목록을 조회합니다.")
    @GetMapping("/v1/{id}/trademark")
    public ResponseEntity<ApiResponse<ShopImageStatusResponse>> getTrademark(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopImageStatusResponse response = shopTrademarkQueryService.getTrademarkStatus(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상표 이미지 변경요청", description = "가게의 상표 이미지 변경을 요청합니다. (JPG, 900KB 이하, 최소 560x560, 1:1 비율)")
    @PostMapping(value = "/v1/{id}/trademark/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> requestTrademarkChange(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Parameter(description = "상표 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        ShopTrademarkChangeRequestCommand command = ShopTrademarkChangeRequestCommand.of(userDetails.getCeoId(), id);
        Long requestId = shopTrademarkCommandUseCase.requestTrademarkChange(command, file);
        return ResponseEntity.ok(ApiResponse.success(requestId));
    }

    @Operation(summary = "대표이미지 현황 조회", description = "가게의 현재 대표이미지와 변경 요청 상태 목록을 조회합니다.")
    @GetMapping("/v1/{id}/thumbnail")
    public ResponseEntity<ApiResponse<ShopImageStatusResponse>> getThumbnail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopImageStatusResponse response = shopTrademarkQueryService.getThumbnailStatus(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "대표이미지 변경요청", description = "가게의 대표이미지 변경을 요청합니다. (JPG/PNG, 10MB 이하, 최소 700x700)")
    @PostMapping(value = "/v1/{id}/thumbnail/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> requestThumbnailChange(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Parameter(description = "대표이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        ShopThumbnailChangeRequestCommand command = ShopThumbnailChangeRequestCommand.of(userDetails.getCeoId(), id);
        Long requestId = shopTrademarkCommandUseCase.requestThumbnailChange(command, file);
        return ResponseEntity.ok(ApiResponse.success(requestId));
    }
}
