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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductDeleteRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductUpdateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductDetailResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCreateCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCreateUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductDeleteCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductDeleteUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductUpdateCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductUpdateUseCase;
import com.tastyhouse.ceoapi.product.application.service.ProductQueryService;

/**
 * 점주 메뉴 CRUD API.
 *
 * <p>모든 핸들러가 body의 {@code shopId}로 소유권을 검증한다 — 경로에 shopId가 없다는 이유로 검증을
 * 생략하면 IDOR가 된다(이 저장소의 배달가능지역 삭제 선례).
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Product", description = "점주 메뉴 CRUD API")
@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductQueryService productQueryService;
    private final ProductCreateUseCase productCreateUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;
    private final ProductDeleteUseCase productDeleteUseCase;

    public ProductApiController(
        ProductQueryService productQueryService,
        ProductCreateUseCase productCreateUseCase,
        ProductUpdateUseCase productUpdateUseCase,
        ProductDeleteUseCase productDeleteUseCase
    ) {
        this.productQueryService = productQueryService;
        this.productCreateUseCase = productCreateUseCase;
        this.productUpdateUseCase = productUpdateUseCase;
        this.productDeleteUseCase = productDeleteUseCase;
    }

    @Operation(summary = "메뉴 상세 조회",
        description = "S2(메뉴 상세) 화면이 쓰는 단건 조회입니다. 노출기간·이미지·연결된 옵션그룹은 각각 "
            + "별도 API가 담당하므로 이 응답에 포함되지 않습니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductDetailResponse response = productQueryService.getProduct(userDetails.getCeoId(), request.shopId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 등록",
        description = "생성된 메뉴 ID만 반환합니다. 노출 순서는 서버가 그룹 맨 뒤로 채우며, 이미지·채식은 "
            + "승인 워크플로가 따로 담당합니다. links를 지정하면 여러 가게 메뉴판에 함께 노출되고, "
            + "생략하면 shopId 가게 하나에만 연결됩니다(기존 동작).")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createProduct(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateCommand command = request.toCommand(userDetails.getCeoId());
        Long productId = productCreateUseCase.createProduct(command);
        return ResponseEntity.ok(ApiResponse.success(productId));
    }

    @Operation(summary = "메뉴 정보 변경",
        description = "메뉴명은 자기 자신을 제외한 중복 검사를 거칩니다. 메뉴그룹을 바꾸면 도착 그룹 기준으로 "
            + "노출 순서가 다시 매겨집니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        productUpdateUseCase.updateProduct(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 일괄 삭제",
        description = "소프트 삭제입니다. 메뉴판에 노출 메뉴 1개와 사장님 추천 메뉴 1개가 남아야 하며, "
            + "제약에 걸린 메뉴는 200 응답의 failed에 담기고 나머지는 정상 삭제됩니다.")
    @DeleteMapping("/v1")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> deleteProducts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductDeleteRequest request
    ) {
        ProductDeleteCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productDeleteUseCase.deleteProducts(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
