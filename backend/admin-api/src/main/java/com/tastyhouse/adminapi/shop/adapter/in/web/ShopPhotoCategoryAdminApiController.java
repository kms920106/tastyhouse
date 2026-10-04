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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopPhotoCategoryImageSaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopPhotoCategorySaveRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryResponse;

@Tag(name = "Shop Photo Category Admin", description = "가게 포토 카테고리 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopPhotoCategoryAdminApiController {

    private final ShopPhotoCategoryCreateUseCase shopPhotoCategoryCreateUseCase;
    private final ShopPhotoCategoryUpdateUseCase shopPhotoCategoryUpdateUseCase;
    private final ShopPhotoCategoryDeleteUseCase shopPhotoCategoryDeleteUseCase;
    private final ShopPhotoCategoryImageCreateUseCase shopPhotoCategoryImageCreateUseCase;
    private final ShopPhotoCategoryImageUpdateUseCase shopPhotoCategoryImageUpdateUseCase;
    private final ShopPhotoCategoryImageDeleteUseCase shopPhotoCategoryImageDeleteUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopPhotoCategoryAdminApiController(
        ShopPhotoCategoryCreateUseCase shopPhotoCategoryCreateUseCase,
        ShopPhotoCategoryUpdateUseCase shopPhotoCategoryUpdateUseCase,
        ShopPhotoCategoryDeleteUseCase shopPhotoCategoryDeleteUseCase,
        ShopPhotoCategoryImageCreateUseCase shopPhotoCategoryImageCreateUseCase,
        ShopPhotoCategoryImageUpdateUseCase shopPhotoCategoryImageUpdateUseCase,
        ShopPhotoCategoryImageDeleteUseCase shopPhotoCategoryImageDeleteUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopPhotoCategoryCreateUseCase = shopPhotoCategoryCreateUseCase;
        this.shopPhotoCategoryUpdateUseCase = shopPhotoCategoryUpdateUseCase;
        this.shopPhotoCategoryDeleteUseCase = shopPhotoCategoryDeleteUseCase;
        this.shopPhotoCategoryImageCreateUseCase = shopPhotoCategoryImageCreateUseCase;
        this.shopPhotoCategoryImageUpdateUseCase = shopPhotoCategoryImageUpdateUseCase;
        this.shopPhotoCategoryImageDeleteUseCase = shopPhotoCategoryImageDeleteUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "포토 카테고리 목록 조회", description = "가게의 포토 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getPhotoCategories(@PathVariable Long id) {
        List<ShopPhotoCategoryResponse> response = shopQueryUseCase.getPhotoCategories(id).stream()
            .map(ShopPhotoCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 등록", description = "가게에 포토 카테고리를 등록합니다.")
    @PostMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategory(
        @PathVariable Long id,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        ShopPhotoCategoryCreateCommand command = request.toCreateCommand(id);
        Long categoryId = shopPhotoCategoryCreateUseCase.createPhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(categoryId));
    }

    @Operation(summary = "포토 카테고리 수정", description = "등록된 포토 카테고리를 수정합니다.")
    @PutMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        ShopPhotoCategoryUpdateCommand command = request.toUpdateCommand(categoryId);
        shopPhotoCategoryUpdateUseCase.updatePhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 삭제", description = "등록된 포토 카테고리를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategory(@PathVariable Long categoryId) {
        ShopPhotoCategoryDeleteCommand command = ShopPhotoCategoryDeleteCommand.of(categoryId);
        shopPhotoCategoryDeleteUseCase.deletePhotoCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 목록 조회", description = "포토 카테고리에 속한 이미지 목록을 조회합니다.")
    @GetMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryImageItemResponse>>> getPhotoCategoryImages(@PathVariable Long categoryId) {
        List<ShopPhotoCategoryImageItemResponse> response = shopQueryUseCase.getPhotoCategoryImages(categoryId).stream()
            .map(ShopPhotoCategoryImageItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 이미지 등록", description = "포토 카테고리에 이미지를 등록합니다.")
    @PostMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategoryImage(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        ShopPhotoCategoryImageCreateCommand command = request.toCreateCommand(categoryId);
        Long imageId = shopPhotoCategoryImageCreateUseCase.createPhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }

    @Operation(summary = "포토 카테고리 이미지 수정", description = "등록된 포토 카테고리 이미지를 수정합니다.")
    @PutMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategoryImage(
        @PathVariable Long imageId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        ShopPhotoCategoryImageUpdateCommand command = request.toUpdateCommand(imageId);
        shopPhotoCategoryImageUpdateUseCase.updatePhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 삭제", description = "등록된 포토 카테고리 이미지를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategoryImage(@PathVariable Long imageId) {
        ShopPhotoCategoryImageDeleteCommand command = ShopPhotoCategoryImageDeleteCommand.of(imageId);
        shopPhotoCategoryImageDeleteUseCase.deletePhotoCategoryImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
