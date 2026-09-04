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
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductVegetarianRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductVegetarianStatusResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductVegetarianClearCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductVegetarianCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductVegetarianRequestCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductVegetarianQueryUseCase;

/**
 * 점주 메뉴 채식 설정 API.
 *
 * <p><b>점주가 직접 켤 수 없다.</b> 재료를 근거로 신청만 하고 관리자가 판정해야 반영된다 — 채식 표기는
 * 알레르기·신념과 직결돼 오표기의 대가가 크다. 반대로 <b>해제는 승인 없이 즉시</b> 반영된다(잘못된 표기를
 * 즉시 내릴 수 있어야 하고, 그 방향에는 오표기 위험이 없다).
 *
 * <p>채식 메뉴를 등록할 수 없는 가게 카테고리(돈까스/회/일식, 고기/구이 등)는 신청 자체를 거부한다.
 */
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
