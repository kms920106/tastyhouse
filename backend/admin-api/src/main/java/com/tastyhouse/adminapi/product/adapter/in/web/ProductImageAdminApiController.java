package com.tastyhouse.adminapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.product.port.in.ProductImageCreateCommand;
import com.tastyhouse.application.product.port.in.ProductImageCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductImageManagementListQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductImageCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductImagesResponse;

@Tag(name = "Product Image Admin", description = "상품 이미지 관리자 API")
@RestController
@RequestMapping("/api/products")
class ProductImageAdminApiController {

    private final ProductImageManagementListQueryUseCase productImageManagementListQueryUseCase;
    private final ProductImageCreateUseCase productImageCreateUseCase;

    public ProductImageAdminApiController(
        ProductImageManagementListQueryUseCase productImageManagementListQueryUseCase,
        ProductImageCreateUseCase productImageCreateUseCase
    ) {
        this.productImageManagementListQueryUseCase = productImageManagementListQueryUseCase;
        this.productImageCreateUseCase = productImageCreateUseCase;
    }

    @Operation(summary = "상품 이미지 목록 조회", description = "상품에 등록된 이미지 URL 목록을 조회합니다.")
    @GetMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<ProductImagesResponse>> getProductImages(@PathVariable Long id) {
        ProductImagesResponse response = ProductImagesResponse.from(productImageManagementListQueryUseCase.getProductImages(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 이미지 등록", description = "사전 업로드된 파일을 상품 이미지로 등록하고, 등록된 이미지의 ID를 반환합니다.")
    @PostMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<Long>> createProductImage(
        @PathVariable Long id,
        @Valid @RequestBody ProductImageCreateRequest request
    ) {
        ProductImageCreateCommand command = request.toCommand(id);
        Long imageId = productImageCreateUseCase.createProductImage(command);
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }
}
