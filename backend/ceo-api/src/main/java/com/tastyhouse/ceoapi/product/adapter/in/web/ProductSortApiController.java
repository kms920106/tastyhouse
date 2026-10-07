package com.tastyhouse.ceoapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.product.port.in.ProductCategoryReorderCommand;
import com.tastyhouse.application.product.port.in.ProductCategoryReorderUseCase;
import com.tastyhouse.application.product.port.in.ProductRelocateCommand;
import com.tastyhouse.application.product.port.in.ProductRelocateUseCase;
import com.tastyhouse.application.product.port.in.ProductReorderCommand;
import com.tastyhouse.application.product.port.in.ProductReorderUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategoryOrderRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCategoryRelocateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOrderRequest;

@Tag(name = "Ceo Product Sort", description = "점주 메뉴그룹·메뉴 순서 변경 API")
@RestController
@RequestMapping("/api/products")
class ProductSortApiController {

    private final ProductCategoryReorderUseCase productCategoryReorderUseCase;
    private final ProductReorderUseCase productReorderUseCase;
    private final ProductRelocateUseCase productRelocateUseCase;

    public ProductSortApiController(
        ProductCategoryReorderUseCase productCategoryReorderUseCase,
        ProductReorderUseCase productReorderUseCase,
        ProductRelocateUseCase productRelocateUseCase
    ) {
        this.productCategoryReorderUseCase = productCategoryReorderUseCase;
        this.productReorderUseCase = productReorderUseCase;
        this.productRelocateUseCase = productRelocateUseCase;
    }

    @Operation(summary = "메뉴그룹 순서 변경",
        description = "가게의 메뉴그룹 전체를 화면 순서대로 나열해 보냅니다. 요청 ID 집합이 가게의 현재 "
            + "메뉴그룹 집합과 다르면 PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH로 거부됩니다.")
    @PutMapping("/v1/categories/order")
    public ResponseEntity<ApiResponse<Void>> reorderProductCategories(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductCategoryOrderRequest request
    ) {
        ProductCategoryReorderCommand command = request.toCommand(userDetails.getCeoId());
        productCategoryReorderUseCase.reorderProductCategories(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "그룹 내 메뉴 순서 변경",
        description = "productCategoryId를 비우면 미분류 메뉴 목록이 대상입니다. 요청 ID 집합이 그 그룹의 "
            + "현재 메뉴 집합과 다르면 PRODUCT_ORDER_TARGET_MISMATCH로 거부됩니다.")
    @PutMapping("/v1/order")
    public ResponseEntity<ApiResponse<Void>> reorderProducts(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOrderRequest request
    ) {
        ProductReorderCommand command = request.toCommand(userDetails.getCeoId());
        productReorderUseCase.reorderProducts(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 그룹 이동",
        description = "targetOrderedProductIds로 도착 그룹의 최종 순서를 함께 보냅니다(드래그로 놓은 위치). "
            + "출발 그룹의 순서도 서버가 함께 0..N-1로 재정규화합니다.")
    @PutMapping("/v1/category")
    public ResponseEntity<ApiResponse<Void>> relocateProducts(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductCategoryRelocateRequest request
    ) {
        ProductRelocateCommand command = request.toCommand(userDetails.getCeoId());
        productRelocateUseCase.relocateProducts(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
