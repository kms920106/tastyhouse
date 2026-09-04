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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductExposureRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductExposureResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductExposureClearCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductExposureCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductExposureReplaceCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductExposureQueryUseCase;

/**
 * 점주 메뉴 노출기간 관리 API.
 *
 * <p>노출 = {@code visible} AND 기간 AND 요일·시간대다. 품절은 직교하므로 이 API가 다루지 않는다 —
 * 품절 메뉴는 목록에 남은 채 '품절' 뱃지만 붙는다.
 *
 * <p>기간·요일·시간대는 <b>replace-all</b>(PUT)로만 바꾼다 — 요일 묶음과 개별 요일의 혼용 금지가
 * 집합 전체를 봐야 판정되는 규칙이라, 행 단위 CRUD를 열면 중간 상태가 반드시 규칙을 위반한다.
 *
 * <p>모든 핸들러가 body 또는 query의 {@code shopId}로 소유권을 검증한다.
 * 역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당한다.
 */
@Tag(name = "Ceo Product Exposure", description = "점주 메뉴 노출기간 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductExposureApiController {

    private final ProductExposureQueryUseCase productExposureQueryService;
    private final ProductExposureCommandUseCase productExposureCommandUseCase;

    public ProductExposureApiController(
        ProductExposureQueryUseCase productExposureQueryService,
        ProductExposureCommandUseCase productExposureCommandUseCase
    ) {
        this.productExposureQueryService = productExposureQueryService;
        this.productExposureCommandUseCase = productExposureCommandUseCase;
    }

    @Operation(summary = "메뉴 노출기간 조회",
        description = "설정된 기간·요일·시간대와 함께 지금 노출 중인지(exposedNow)와 그 사유(hiddenReason)를 반환합니다.")
    @GetMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<ProductExposureResponse>> getProductExposure(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductExposureResponse response = ProductExposureResponse.from(productExposureQueryService.getExposure( userDetails.getCeoId(), request.shopId(), id ));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 노출기간 설정",
        description = "기간과 요일·시간대를 통째로 치환합니다. hours를 빈 배열로 보내면 요일·시간 제약이 사라집니다.")
    @PutMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<Void>> changeProductExposure(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductExposureRequest request
    ) {
        ProductExposureReplaceCommand command = request.toCommand(userDetails.getCeoId(), id);
        productExposureCommandUseCase.replaceExposure(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 노출기간 해제",
        description = "기간·요일·시간대를 모두 비워 상시 노출로 되돌립니다. 숨김 상태는 바뀌지 않습니다.")
    @DeleteMapping("/v1/{id}/exposure")
    public ResponseEntity<ApiResponse<Void>> clearProductExposure(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductExposureClearCommand command = request.toExposureClearCommand(userDetails.getCeoId(), id);
        productExposureCommandUseCase.clearExposure(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
