package com.tastyhouse.ceoapi.product.adapter.in.web;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductVegetarianRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductVegetarianStatusResponse;
import com.tastyhouse.application.product.port.in.ProductVegetarianClearCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductVegetarianRequestCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianQueryUseCase;

@Tag(name = "Ceo Product Vegetarian", description = "점주 메뉴 채식 설정 API")
@RestController
@RequestMapping("/api/products")
public class ProductVegetarianApiController {
    private final ProductVegetarianQueryUseCase productVegetarianQueryService;
    private final ProductVegetarianCommandUseCase productVegetarianCommandUseCase;

    public ProductVegetarianApiController(
        ProductVegetarianQueryUseCase productVegetarianQueryService,
        ProductVegetarianCommandUseCase productVegetarianCommandUseCase
    ) {
        this.productVegetarianQueryService = productVegetarianQueryService;
        this.productVegetarianCommandUseCase = productVegetarianCommandUseCase;
    }

    @Operation(summary = "메뉴 채식 설정 조회",
        description = "현재 반영된 채식 단계와 검수 요청 이력을 함께 반환합니다. 승인 전 요청이 있어도 "
            + "vegetarianType은 바뀌지 않습니다.")
    @GetMapping("/v1/{id}/vegetarian")
    public ResponseEntity<ApiResponse<ProductVegetarianStatusResponse>> getProductVegetarian(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductVegetarianStatusResponse response = ProductVegetarianStatusResponse.from(productVegetarianQueryService.getVegetarianStatus( userDetails.getCeoId(), request.shopId(), id ));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 채식 설정 요청",
        description = "포함 재료는 검수의 근거이므로 필수입니다. 채식 불가 카테고리 가게이거나 검수 대기 중인 "
            + "요청이 있으면 거부됩니다.")
    @PostMapping("/v1/{id}/vegetarian")
    public ResponseEntity<ApiResponse<Long>> requestProductVegetarian(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductVegetarianRequest request
    ) {
        ProductVegetarianRequestCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long requestId = productVegetarianCommandUseCase.requestVegetarian(command);
        return ResponseEntity.ok(ApiResponse.success(requestId));
    }

    @Operation(summary = "메뉴 채식 해제",
        description = "승인을 거치지 않고 즉시 해제됩니다. 이미 해제 상태여도 실패가 아닙니다(멱등).")
    @DeleteMapping("/v1/{id}/vegetarian")
    public ResponseEntity<ApiResponse<Void>> clearProductVegetarian(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductVegetarianClearCommand command = request.toVegetarianClearCommand(userDetails.getCeoId(), id);
        productVegetarianCommandUseCase.clearVegetarian(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
