package com.tastyhouse.adminapi.product;

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
import com.tastyhouse.adminapi.product.request.ProductCategoryCreateRequest;
import com.tastyhouse.adminapi.product.request.ProductCategorySearchRequest;
import com.tastyhouse.adminapi.product.request.ProductCreateRequest;
import com.tastyhouse.adminapi.product.request.ProductImageCreateRequest;
import com.tastyhouse.adminapi.product.request.ProductOptionCreateRequest;
import com.tastyhouse.adminapi.product.request.ProductOptionGroupCreateRequest;
import com.tastyhouse.adminapi.product.request.ProductSearchRequest;
import com.tastyhouse.adminapi.product.request.ProductUpdateRequest;
import com.tastyhouse.adminapi.product.response.ProductCategoryResponse;
import com.tastyhouse.adminapi.product.response.ProductDetailResponse;
import com.tastyhouse.adminapi.product.response.ProductImagesResponse;
import com.tastyhouse.adminapi.product.response.ProductListItemResponse;
import com.tastyhouse.adminapi.product.response.ProductOptionGroupsResponse;

@Tag(name = "Product Admin", description = "상품 관리자 API")
@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductApiController(ProductCommandService productCommandService, ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ProductListItemResponse>>> getProducts(
        @Valid @ModelAttribute ProductSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ProductListItemResponse> pageResponse = productQueryService.getProducts(
            search.shopId(), search.productCategoryId(), search.name(), search.visible(), search.soldOut(),
            pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Long id = productCommandService.createProduct(
            request.shopId(), request.productCategoryId(), request.name(), request.description(),
            request.originalPrice(), request.discountPrice(), request.discountRate(),
            request.rating(), request.reviewCount(), request.representative(), request.spiciness(),
            request.soldOut(), request.visible(), request.sort()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        ProductDetailResponse response = productQueryService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 수정", description = "기존 상품을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        productCommandService.updateProduct(
            id, request.productCategoryId(), request.name(), request.description(),
            request.originalPrice(), request.discountPrice(), request.discountRate(),
            request.representative(), request.spiciness(), request.soldOut(), request.visible(), request.sort()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 품절 처리", description = "상품을 품절 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/sold-out")
    public ResponseEntity<ApiResponse<Void>> markSoldOut(@PathVariable Long id) {
        productCommandService.markSoldOut(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 비활성화", description = "상품을 비노출 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long id) {
        productCommandService.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 옵션 조회", description = "상품의 옵션그룹과 옵션 목록을 조회합니다. (공통 옵션그룹 병합 포함)")
    @GetMapping("/v1/{id}/options")
    public ResponseEntity<ApiResponse<ProductOptionGroupsResponse>> getProductOptions(@PathVariable Long id) {
        ProductOptionGroupsResponse response = productQueryService.getProductOptions(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 옵션그룹 등록", description = "상품에 새로운 옵션그룹을 등록합니다.")
    @PostMapping("/v1/{id}/option-groups")
    public ResponseEntity<ApiResponse<Long>> createProductOptionGroup(
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupCreateRequest request
    ) {
        Long optionGroupId = productCommandService.createProductOptionGroup(
            id, request.name(), request.description(), request.required(), request.multipleSelect(),
            request.minSelect(), request.maxSelect(), request.sort(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(optionGroupId));
    }

    @Operation(summary = "상품 옵션 등록", description = "옵션그룹에 새로운 옵션을 등록하고, 등록된 옵션의 ID를 반환합니다.")
    @PostMapping("/v1/option-groups/{groupId}/options")
    public ResponseEntity<ApiResponse<Long>> createProductOption(
        @PathVariable Long groupId,
        @Valid @RequestBody ProductOptionCreateRequest request
    ) {
        Long optionId = productCommandService.createProductOption(
            groupId, request.name(), request.additionalPrice(), request.sort(), request.soldOut(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(optionId));
    }

    @Operation(summary = "상품 이미지 목록 조회", description = "상품에 등록된 이미지 URL 목록을 조회합니다.")
    @GetMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<ProductImagesResponse>> getProductImages(@PathVariable Long id) {
        ProductImagesResponse response = productQueryService.getProductImages(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 이미지 등록", description = "사전 업로드된 파일을 상품 이미지로 등록하고, 등록된 이미지의 ID를 반환합니다.")
    @PostMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<Long>> createProductImage(
        @PathVariable Long id,
        @Valid @RequestBody ProductImageCreateRequest request
    ) {
        Long imageId = productCommandService.createProductImage(id, request.imageFileId(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }

    @Operation(summary = "상품 카테고리 목록 조회", description = "매장의 상품 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getProductCategories(
        @Valid @ModelAttribute ProductCategorySearchRequest search
    ) {
        List<ProductCategoryResponse> response = productQueryService.getProductCategories(search.shopId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상품 카테고리 등록", description = "새로운 상품 카테고리를 등록합니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createProductCategory(@Valid @RequestBody ProductCategoryCreateRequest request) {
        Long id = productCommandService.createProductCategory(request.shopId(), request.name(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}
