package com.tastyhouse.adminapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.product.port.in.ProductCategoryCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductCategoryManagementCreateCommand;
import com.tastyhouse.application.product.port.in.ProductManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCategoryCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCategorySearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductCategoryResponse;

@Tag(name = "Product Category Admin", description = "상품 카테고리 관리자 API")
@RestController
@RequestMapping("/api/products")
public class ProductCategoryAdminApiController {

    private final ProductCategoryCreateUseCase productCategoryCreateUseCase;
    private final ProductManagementQueryUseCase productQueryUseCase;

    public ProductCategoryAdminApiController(ProductCategoryCreateUseCase productCategoryCreateUseCase, ProductManagementQueryUseCase productQueryUseCase) {
        this.productCategoryCreateUseCase = productCategoryCreateUseCase;
        this.productQueryUseCase = productQueryUseCase;
    }

    @Operation(summary = "상품 카테고리 목록 조회", description = "매장의 상품 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getProductCategories(
        @Valid @ModelAttribute ProductCategorySearchRequest search
    ) {
        List<ProductCategoryResponse> response = productQueryUseCase.getProductCategories(search.shopId()).stream()
            .map(ProductCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 카테고리 등록", description = "새로운 상품 카테고리를 등록합니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createProductCategory(@Valid @RequestBody ProductCategoryCreateRequest request) {
        ProductCategoryManagementCreateCommand command = request.toCommand();
        Long id = productCategoryCreateUseCase.createProductCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}
