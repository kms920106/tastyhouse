package com.tastyhouse.adminapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.product.port.in.ProductDeactivateCommand;
import com.tastyhouse.application.product.port.in.ProductDeactivateUseCase;
import com.tastyhouse.application.product.port.in.ProductManagementCreateCommand;
import com.tastyhouse.application.product.port.in.ProductManagementCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductManagementQueryUseCase;
import com.tastyhouse.application.product.port.in.ProductManagementUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductManagementUpdateUseCase;
import com.tastyhouse.application.product.port.in.ProductSoldOutManagementCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutManagementUseCase;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductSearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductUpdateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductListItemResponse;

@Tag(name = "Product Admin", description = "상품 관리자 API")
@RestController
@RequestMapping("/api/products")
class ProductApiController {

    private final ProductManagementCreateUseCase productCreateUseCase;
    private final ProductManagementUpdateUseCase productUpdateUseCase;
    private final ProductSoldOutManagementUseCase productSoldOutUseCase;
    private final ProductDeactivateUseCase productDeactivateUseCase;
    private final ProductManagementQueryUseCase productQueryUseCase;

    public ProductApiController(
        ProductManagementCreateUseCase productCreateUseCase,
        ProductManagementUpdateUseCase productUpdateUseCase,
        ProductSoldOutManagementUseCase productSoldOutUseCase,
        ProductDeactivateUseCase productDeactivateUseCase,
        ProductManagementQueryUseCase productQueryUseCase
    ) {
        this.productCreateUseCase = productCreateUseCase;
        this.productUpdateUseCase = productUpdateUseCase;
        this.productSoldOutUseCase = productSoldOutUseCase;
        this.productDeactivateUseCase = productDeactivateUseCase;
        this.productQueryUseCase = productQueryUseCase;
    }

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ProductListItemResponse>>> getProducts(
        @Valid @ModelAttribute ProductSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ProductListItemResult> pageResult = productQueryUseCase.getProducts(
            search.shopId(), search.productCategoryId(), search.name(), search.visible(), search.soldOut(),
            pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<ProductListItemResponse> pageResponse = PaginationResponse.from(pageResult.map(ProductListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductManagementCreateCommand command = request.toCommand();
        Long id = productCreateUseCase.createProduct(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        ProductDetailResponse response = ProductDetailResponse.from(productQueryUseCase.getProduct(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 수정", description = "기존 상품을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductManagementUpdateCommand command = request.toCommand(id);
        productUpdateUseCase.updateProduct(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 품절 처리", description = "상품을 품절 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/sold-out")
    public ResponseEntity<ApiResponse<Void>> markSoldOut(@PathVariable Long id) {
        ProductSoldOutManagementCommand command = ProductSoldOutManagementCommand.of(id);
        productSoldOutUseCase.markSoldOut(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 비활성화", description = "상품을 비노출 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long id) {
        ProductDeactivateCommand command = ProductDeactivateCommand.of(id);
        productDeactivateUseCase.deactivateProduct(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
