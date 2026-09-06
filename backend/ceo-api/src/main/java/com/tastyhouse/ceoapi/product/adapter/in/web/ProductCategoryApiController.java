package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategoryCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategoryDeleteRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategorySearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategoryUpdateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductCategoryResponse;
import com.tastyhouse.application.product.port.in.ProductCategoryCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductCategoryOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryQueryUseCase;

@Tag(name = "Ceo Product Category", description = "점주 메뉴그룹 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductCategoryApiController {
    private final ProductCategoryQueryUseCase productCategoryQueryService;
    private final ProductCategoryCommandUseCase productCategoryCommandUseCase;

    public ProductCategoryApiController(
        ProductCategoryQueryUseCase productCategoryQueryService,
        ProductCategoryCommandUseCase productCategoryCommandUseCase
    ) {
        this.productCategoryQueryService = productCategoryQueryService;
        this.productCategoryCommandUseCase = productCategoryCommandUseCase;
    }

    @Operation(summary = "메뉴그룹 목록 조회", description = "노출 순서(sort) 오름차순으로 반환합니다.")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getProductCategories(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductCategorySearchRequest request
    ) {
        List<ProductCategoryResponse> response = productCategoryQueryService.getProductCategories( userDetails.getCeoId(), request.shopId() ).stream()
            .map(ProductCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴그룹 추가",
        description = "생성된 메뉴그룹 ID만 반환합니다. 노출 순서는 서버가 목록 맨 뒤로 채웁니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createProductCategory(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductCategoryCreateRequest request
    ) {
        ProductCategoryOwnerCreateCommand command = request.toCommand(userDetails.getCeoId());
        Long productCategoryId = productCategoryCommandUseCase.createProductCategory(command);
        return ResponseEntity.ok(ApiResponse.success(productCategoryId));
    }

    @Operation(summary = "메뉴그룹명·설명 변경", description = "노출 순서는 이 경로로 바꾸지 않습니다.")
    @PutMapping("/v1/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProductCategory(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductCategoryUpdateRequest request
    ) {
        ProductCategoryUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        productCategoryCommandUseCase.updateProductCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴그룹 삭제",
        description = "소속 메뉴가 남아 있으면 거부됩니다(PRODUCT_CATEGORY_HAS_PRODUCTS). 먼저 메뉴를 다른 "
            + "그룹으로 옮기거나 삭제해야 합니다.")
    @DeleteMapping("/v1/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductCategory(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductCategoryDeleteRequest request
    ) {
        ProductCategoryDeleteCommand command = request.toCommand(userDetails.getCeoId(), id);
        productCategoryCommandUseCase.deleteProductCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
