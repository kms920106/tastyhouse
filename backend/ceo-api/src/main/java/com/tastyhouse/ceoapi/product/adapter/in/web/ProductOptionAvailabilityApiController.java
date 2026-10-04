package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.product.port.in.ProductAvailabilityQueryUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionHideCommand;
import com.tastyhouse.application.product.port.in.ProductOptionHideUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutCommand;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUntilChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUntilChangeUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductAvailabilitySearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionHiddenRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionReleaseRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionSoldOutRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionSoldOutUntilRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionAvailabilityGroupResponse;

@Tag(name = "Ceo Product Option Availability", description = "점주 옵션 품절·숨김 관리 API")
@RestController
@RequestMapping("/api/products")
class ProductOptionAvailabilityApiController {

    private final ProductAvailabilityQueryUseCase productAvailabilityQueryUseCase;
    private final ProductOptionSoldOutUseCase productOptionSoldOutUseCase;
    private final ProductOptionHideUseCase productOptionHideUseCase;
    private final ProductOptionReleaseUseCase productOptionReleaseUseCase;
    private final ProductOptionSoldOutUntilChangeUseCase productOptionSoldOutUntilChangeUseCase;

    public ProductOptionAvailabilityApiController(
        ProductAvailabilityQueryUseCase productAvailabilityQueryUseCase,
        ProductOptionSoldOutUseCase productOptionSoldOutUseCase,
        ProductOptionHideUseCase productOptionHideUseCase,
        ProductOptionReleaseUseCase productOptionReleaseUseCase,
        ProductOptionSoldOutUntilChangeUseCase productOptionSoldOutUntilChangeUseCase
    ) {
        this.productAvailabilityQueryUseCase = productAvailabilityQueryUseCase;
        this.productOptionSoldOutUseCase = productOptionSoldOutUseCase;
        this.productOptionHideUseCase = productOptionHideUseCase;
        this.productOptionReleaseUseCase = productOptionReleaseUseCase;
        this.productOptionSoldOutUntilChangeUseCase = productOptionSoldOutUntilChangeUseCase;
    }

    @Operation(summary = "품절·숨김 관리 옵션 목록 조회",
        description = "옵션그룹 단위로 묶어 반환합니다. 일반 옵션그룹과 공통 옵션그룹을 하나의 목록으로 합쳐 "
            + "내려주며, 검색어는 옵션명에 부분일치합니다.")
    @GetMapping("/v1/availability/options")
    public ResponseEntity<ApiResponse<List<ProductOptionAvailabilityGroupResponse>>> getProductOptionAvailability(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductAvailabilitySearchRequest request
    ) {
        List<ProductOptionAvailabilityGroupResponse> response = productAvailabilityQueryUseCase.getProductOptionAvailability( userDetails.getCeoId(), request.shopId(), request.keyword(), request.soldOutOnly(), request.hiddenOnly() ).stream()
            .map(ProductOptionAvailabilityGroupResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 일괄 품절",
        description = "옵션그룹별로 최소 선택 개수만큼은 판매 중이어야 합니다. 제약에 걸린 옵션은 failed에 담깁니다.")
    @PatchMapping("/v1/availability/options/sold-out")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> markOptionsSoldOut(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOptionSoldOutRequest request
    ) {
        ProductOptionSoldOutCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productOptionSoldOutUseCase.markOptionsSoldOut(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 일괄 숨김",
        description = "숨김도 선택 불가로 만들므로 품절과 동일하게 옵션그룹별 최소 선택 개수 제약이 적용됩니다.")
    @PatchMapping("/v1/availability/options/hidden")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> hideOptions(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOptionHiddenRequest request
    ) {
        ProductOptionHideCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productOptionHideUseCase.hideOptions(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 일괄 품절·숨김 해제", description = "해제 방향에는 제약이 없습니다(멱등).")
    @PatchMapping("/v1/availability/options/release")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> releaseOptions(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOptionReleaseRequest request
    ) {
        ProductOptionReleaseCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productOptionReleaseUseCase.releaseOptions(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 품절 기간 일괄 변경", description = "품절 상태가 아닌 대상은 failed에 담깁니다.")
    @PatchMapping("/v1/availability/options/sold-out-until")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> changeOptionsSoldOutUntil(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOptionSoldOutUntilRequest request
    ) {
        ProductOptionSoldOutUntilChangeCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productOptionSoldOutUntilChangeUseCase.changeOptionsSoldOutUntil(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
