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

import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopFoodTypeCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeResponse;

@Tag(name = "Shop Food Type Admin", description = "가게 음식종류 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopFoodTypeAdminApiController {

    private final ShopFoodTypeCategoryCreateUseCase shopFoodTypeCategoryCreateUseCase;
    private final ShopFoodTypeCategoryUpdateUseCase shopFoodTypeCategoryUpdateUseCase;
    private final ShopFoodTypeAssignUseCase shopFoodTypeAssignUseCase;
    private final ShopFoodTypeUnassignUseCase shopFoodTypeUnassignUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopFoodTypeAdminApiController(
        ShopFoodTypeCategoryCreateUseCase shopFoodTypeCategoryCreateUseCase,
        ShopFoodTypeCategoryUpdateUseCase shopFoodTypeCategoryUpdateUseCase,
        ShopFoodTypeAssignUseCase shopFoodTypeAssignUseCase,
        ShopFoodTypeUnassignUseCase shopFoodTypeUnassignUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopFoodTypeCategoryCreateUseCase = shopFoodTypeCategoryCreateUseCase;
        this.shopFoodTypeCategoryUpdateUseCase = shopFoodTypeCategoryUpdateUseCase;
        this.shopFoodTypeAssignUseCase = shopFoodTypeAssignUseCase;
        this.shopFoodTypeUnassignUseCase = shopFoodTypeUnassignUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "음식종류 카테고리 목록 조회", description = "음식종류 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeCategoryResponse>>> getFoodTypeCategories() {
        List<ShopFoodTypeCategoryResponse> response = shopQueryUseCase.getFoodTypeCategories().stream()
            .map(ShopFoodTypeCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "음식종류 카테고리 등록", description = "음식종류 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<Long>> createFoodTypeCategory(@Valid @RequestBody ShopFoodTypeCategoryCreateRequest request) {
        ShopFoodTypeCategoryCreateCommand command = request.toCommand();
        Long id = shopFoodTypeCategoryCreateUseCase.createFoodTypeCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "음식종류 카테고리 수정", description = "음식종류 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/food-type-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateFoodTypeCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopFoodTypeCategoryUpdateRequest request
    ) {
        ShopFoodTypeCategoryUpdateCommand command = request.toCommand(categoryId);
        shopFoodTypeCategoryUpdateUseCase.updateFoodTypeCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 음식종류 목록 조회", description = "가게에 지정된 음식종류 목록을 조회합니다.")
    @GetMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeResponse>>> getShopFoodTypes(@PathVariable Long id) {
        List<ShopFoodTypeResponse> response = shopQueryUseCase.getShopFoodTypes(id).stream()
            .map(ShopFoodTypeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 음식종류 지정", description = "가게에 음식종류를 지정합니다.")
    @PostMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<Long>> assignFoodType(
        @PathVariable Long id,
        @Valid @RequestBody ShopFoodTypeAssignRequest request
    ) {
        ShopFoodTypeAssignCommand command = request.toCommand(id);
        Long foodTypeId = shopFoodTypeAssignUseCase.assignFoodType(command);
        return ResponseEntity.ok(ApiResponse.success(foodTypeId));
    }

    @Operation(summary = "가게 음식종류 해제", description = "가게에 지정된 음식종류를 해제합니다.")
    @DeleteMapping("/v1/{id}/food-types/{foodTypeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignFoodType(
        @PathVariable Long id,
        @PathVariable Long foodTypeCategoryId
    ) {
        ShopFoodTypeUnassignCommand command = ShopFoodTypeUnassignCommand.of(id, foodTypeCategoryId);
        shopFoodTypeUnassignUseCase.unassignFoodType(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
