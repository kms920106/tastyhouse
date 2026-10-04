package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.application.shop.port.in.ShopAmenityAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityManagementAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityManagementUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityUnassignUseCase;
import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopAmenityCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityResponse;

@Tag(name = "Shop Amenity Admin", description = "가게 편의시설 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopAmenityAdminApiController {

    private final ShopAmenityCategoryCreateUseCase shopAmenityCategoryCreateUseCase;
    private final ShopAmenityCategoryUpdateUseCase shopAmenityCategoryUpdateUseCase;
    private final ShopAmenityAssignUseCase shopAmenityAssignUseCase;
    private final ShopAmenityUnassignUseCase shopAmenityUnassignUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopAmenityAdminApiController(
        ShopAmenityCategoryCreateUseCase shopAmenityCategoryCreateUseCase,
        ShopAmenityCategoryUpdateUseCase shopAmenityCategoryUpdateUseCase,
        ShopAmenityAssignUseCase shopAmenityAssignUseCase,
        ShopAmenityUnassignUseCase shopAmenityUnassignUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopAmenityCategoryCreateUseCase = shopAmenityCategoryCreateUseCase;
        this.shopAmenityCategoryUpdateUseCase = shopAmenityCategoryUpdateUseCase;
        this.shopAmenityAssignUseCase = shopAmenityAssignUseCase;
        this.shopAmenityUnassignUseCase = shopAmenityUnassignUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "편의시설 카테고리 목록 조회", description = "편의시설 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<List<ShopAmenityCategoryResponse>>> getAmenityCategories() {
        List<ShopAmenityCategoryResponse> response = shopQueryUseCase.getAmenityCategories().stream()
            .map(ShopAmenityCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "편의시설 카테고리 등록", description = "편의시설 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<Long>> createAmenityCategory(@Valid @RequestBody ShopAmenityCategoryCreateRequest request) {
        ShopAmenityCategoryCreateCommand command = request.toCommand();
        Long id = shopAmenityCategoryCreateUseCase.createAmenityCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "편의시설 카테고리 수정", description = "편의시설 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/amenity-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateAmenityCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopAmenityCategoryUpdateRequest request
    ) {
        ShopAmenityCategoryUpdateCommand command = request.toCommand(categoryId);
        shopAmenityCategoryUpdateUseCase.updateAmenityCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 편의시설 목록 조회", description = "가게에 지정된 편의시설 목록을 조회합니다.")
    @GetMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityResponse>>> getShopAmenities(@PathVariable Long id) {
        List<ShopAmenityResponse> response = shopQueryUseCase.getShopAmenities(id).stream()
            .map(ShopAmenityResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 편의시설 지정", description = "가게에 편의시설을 지정합니다.")
    @PostMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<Long>> assignAmenity(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopAmenityAssignRequest request
    ) {
        ShopAmenityManagementAssignCommand command = request.toCommand(userDetails.getPrincipalId(), id);
        Long amenityId = shopAmenityAssignUseCase.assignAmenity(command);
        return ResponseEntity.ok(ApiResponse.success(amenityId));
    }

    @Operation(summary = "가게 편의시설 해제", description = "가게에 지정된 편의시설을 해제합니다.")
    @DeleteMapping("/v1/{id}/amenities/{amenityCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignAmenity(
        @AuthenticationPrincipal AdminUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long amenityCategoryId
    ) {
        ShopAmenityManagementUnassignCommand command = ShopAmenityManagementUnassignCommand.of(userDetails.getPrincipalId(), id, amenityCategoryId);
        shopAmenityUnassignUseCase.unassignAmenity(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
