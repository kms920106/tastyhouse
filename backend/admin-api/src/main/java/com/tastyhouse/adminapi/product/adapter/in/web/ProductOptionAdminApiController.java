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

import com.tastyhouse.application.product.port.in.ProductManagementQueryUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupManagementCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionManagementCreateCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductOptionCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductOptionGroupCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductOptionGroupsResponse;

@Tag(name = "Product Option Admin", description = "상품 옵션 관리자 API")
@RestController
@RequestMapping("/api/products")
class ProductOptionAdminApiController {

    private final ProductOptionGroupCreateUseCase productOptionGroupCreateUseCase;
    private final ProductOptionCreateUseCase productOptionCreateUseCase;
    private final ProductManagementQueryUseCase productQueryUseCase;

    public ProductOptionAdminApiController(
        ProductOptionGroupCreateUseCase productOptionGroupCreateUseCase,
        ProductOptionCreateUseCase productOptionCreateUseCase,
        ProductManagementQueryUseCase productQueryUseCase
    ) {
        this.productOptionGroupCreateUseCase = productOptionGroupCreateUseCase;
        this.productOptionCreateUseCase = productOptionCreateUseCase;
        this.productQueryUseCase = productQueryUseCase;
    }

    @Operation(summary = "상품 옵션 조회", description = "상품의 옵션그룹과 옵션 목록을 조회합니다. (공통 옵션그룹 병합 포함)")
    @GetMapping("/v1/{id}/options")
    public ResponseEntity<ApiResponse<ProductOptionGroupsResponse>> getProductOptions(@PathVariable Long id) {
        ProductOptionGroupsResponse response = ProductOptionGroupsResponse.from(productQueryUseCase.getProductOptions(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 옵션그룹 등록", description = "상품에 새로운 옵션그룹을 등록합니다.")
    @PostMapping("/v1/{id}/option-groups")
    public ResponseEntity<ApiResponse<Long>> createProductOptionGroup(
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupCreateRequest request
    ) {
        ProductOptionGroupManagementCreateCommand command = request.toCommand(id);
        Long optionGroupId = productOptionGroupCreateUseCase.createProductOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(optionGroupId));
    }

    @Operation(summary = "상품 옵션 등록", description = "옵션그룹에 새로운 옵션을 등록하고, 등록된 옵션의 ID를 반환합니다.")
    @PostMapping("/v1/option-groups/{groupId}/options")
    public ResponseEntity<ApiResponse<Long>> createProductOption(
        @PathVariable Long groupId,
        @Valid @RequestBody ProductOptionCreateRequest request
    ) {
        ProductOptionManagementCreateCommand command = request.toCommand(groupId);
        Long optionId = productOptionCreateUseCase.createProductOption(command);
        return ResponseEntity.ok(ApiResponse.success(optionId));
    }
}
