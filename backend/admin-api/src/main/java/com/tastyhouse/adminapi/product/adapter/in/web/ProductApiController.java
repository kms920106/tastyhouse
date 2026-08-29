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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCategoryCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCategorySearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductImageCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductOptionCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductOptionGroupCreateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductSearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductUpdateRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductCategoryResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductImagesResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductListItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductOptionGroupsResponse;
import com.tastyhouse.adminapi.product.application.port.in.ProductCategoryCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductCategoryCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductDeactivateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductDeactivateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductImageCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductImageCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionGroupCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionGroupCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductSoldOutCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductSoldOutUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductUpdateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductUpdateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductQueryUseCase;

@Tag(name = "Product Admin", description = "상품 관리자 API")
@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductCreateUseCase productCreateUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;
    private final ProductSoldOutUseCase productSoldOutUseCase;
    private final ProductDeactivateUseCase productDeactivateUseCase;
    private final ProductOptionGroupCreateUseCase productOptionGroupCreateUseCase;
    private final ProductOptionCreateUseCase productOptionCreateUseCase;
    private final ProductImageCreateUseCase productImageCreateUseCase;
    private final ProductCategoryCreateUseCase productCategoryCreateUseCase;
    private final ProductQueryUseCase productQueryUseCase;

    public ProductApiController(
        ProductCreateUseCase productCreateUseCase,
        ProductUpdateUseCase productUpdateUseCase,
        ProductSoldOutUseCase productSoldOutUseCase,
        ProductDeactivateUseCase productDeactivateUseCase,
        ProductOptionGroupCreateUseCase productOptionGroupCreateUseCase,
        ProductOptionCreateUseCase productOptionCreateUseCase,
        ProductImageCreateUseCase productImageCreateUseCase,
        ProductCategoryCreateUseCase productCategoryCreateUseCase,
        ProductQueryUseCase productQueryUseCase
    ) {
        this.productCreateUseCase = productCreateUseCase;
        this.productUpdateUseCase = productUpdateUseCase;
        this.productSoldOutUseCase = productSoldOutUseCase;
        this.productDeactivateUseCase = productDeactivateUseCase;
        this.productOptionGroupCreateUseCase = productOptionGroupCreateUseCase;
        this.productOptionCreateUseCase = productOptionCreateUseCase;
        this.productImageCreateUseCase = productImageCreateUseCase;
        this.productCategoryCreateUseCase = productCategoryCreateUseCase;
        this.productQueryUseCase = productQueryUseCase;
    }

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ProductListItemResponse>>> getProducts(
        @Valid @ModelAttribute ProductSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ProductListItemResponse> pageResponse = productQueryUseCase.getProducts(
            search.shopId(), search.productCategoryId(), search.name(), search.visible(), search.soldOut(),
            pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductCreateCommand command = request.toCommand();
        Long id = productCreateUseCase.createProduct(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        ProductDetailResponse response = productQueryUseCase.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 수정", description = "기존 상품을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateCommand command = request.toCommand(id);
        productUpdateUseCase.updateProduct(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 품절 처리", description = "상품을 품절 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/sold-out")
    public ResponseEntity<ApiResponse<Void>> markSoldOut(@PathVariable Long id) {
        ProductSoldOutCommand command = ProductSoldOutCommand.of(id);
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

    @Operation(summary = "상품 옵션 조회", description = "상품의 옵션그룹과 옵션 목록을 조회합니다. (공통 옵션그룹 병합 포함)")
    @GetMapping("/v1/{id}/options")
    public ResponseEntity<ApiResponse<ProductOptionGroupsResponse>> getProductOptions(@PathVariable Long id) {
        ProductOptionGroupsResponse response = productQueryUseCase.getProductOptions(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 옵션그룹 등록", description = "상품에 새로운 옵션그룹을 등록합니다.")
    @PostMapping("/v1/{id}/option-groups")
    public ResponseEntity<ApiResponse<Long>> createProductOptionGroup(
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupCreateRequest request
    ) {
        ProductOptionGroupCreateCommand command = request.toCommand(id);
        Long optionGroupId = productOptionGroupCreateUseCase.createProductOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(optionGroupId));
    }

    @Operation(summary = "상품 옵션 등록", description = "옵션그룹에 새로운 옵션을 등록하고, 등록된 옵션의 ID를 반환합니다.")
    @PostMapping("/v1/option-groups/{groupId}/options")
    public ResponseEntity<ApiResponse<Long>> createProductOption(
        @PathVariable Long groupId,
        @Valid @RequestBody ProductOptionCreateRequest request
    ) {
        ProductOptionCreateCommand command = request.toCommand(groupId);
        Long optionId = productOptionCreateUseCase.createProductOption(command);
        return ResponseEntity.ok(ApiResponse.success(optionId));
    }

    @Operation(summary = "상품 이미지 목록 조회", description = "상품에 등록된 이미지 URL 목록을 조회합니다.")
    @GetMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<ProductImagesResponse>> getProductImages(@PathVariable Long id) {
        ProductImagesResponse response = productQueryUseCase.getProductImages(id);
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

    @Operation(summary = "상품 카테고리 목록 조회", description = "매장의 상품 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getProductCategories(
        @Valid @ModelAttribute ProductCategorySearchRequest search
    ) {
        List<ProductCategoryResponse> response = productQueryUseCase.getProductCategories(search.shopId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 카테고리 등록", description = "새로운 상품 카테고리를 등록합니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createProductCategory(@Valid @RequestBody ProductCategoryCreateRequest request) {
        ProductCategoryCreateCommand command = request.toCommand();
        Long id = productCategoryCreateUseCase.createProductCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}
