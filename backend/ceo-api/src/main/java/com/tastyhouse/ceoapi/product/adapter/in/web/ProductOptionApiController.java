package com.tastyhouse.ceoapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionDeleteRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionSortRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionUpdateRequest;
import com.tastyhouse.application.product.port.in.ProductOptionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionUpdateCommand;

@Tag(name = "Ceo Product Option", description = "점주 옵션 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductOptionApiController {
    private final ProductOptionCommandUseCase productOptionCommandUseCase;

    public ProductOptionApiController(ProductOptionCommandUseCase productOptionCommandUseCase) {
        this.productOptionCommandUseCase = productOptionCommandUseCase;
    }

    @Operation(summary = "옵션 추가",
        description = "생성된 옵션 ID만 반환합니다. 노출 순서는 서버가 그룹의 맨 뒤로 채웁니다.")
    @PostMapping("/v1/option-groups/{optionGroupId}/options")
    public ResponseEntity<ApiResponse<Long>> createProductOption(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long optionGroupId,
        @Valid @RequestBody ProductOptionCreateRequest request
    ) {
        ProductOptionOwnerCreateCommand command = request.toCommand(userDetails.getCeoId(), optionGroupId);
        Long optionId = productOptionCommandUseCase.createProductOption(command);
        return ResponseEntity.ok(ApiResponse.success(optionId));
    }

    @Operation(summary = "옵션명·추가 금액 변경",
        description = "품절·숨김 상태와 노출 순서는 이 경로로 바꾸지 않습니다.")
    @PutMapping("/v1/options/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProductOption(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionUpdateRequest request
    ) {
        ProductOptionUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        productOptionCommandUseCase.updateProductOption(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "옵션 삭제",
        description = "행을 지우지 않고 감춥니다(소프트 삭제) — 과거 주문에 박제된 옵션 이력을 보존하기 "
            + "위함입니다. 감춘 뒤 남는 판매중 옵션이 그룹의 최소 선택 개수에 못 미치면 거부됩니다"
            + "(PRODUCT_OPTION_MIN_SELECT_VIOLATION).")
    @DeleteMapping("/v1/options/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductOption(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionDeleteRequest request
    ) {
        ProductOptionDeleteCommand command = request.toCommand(userDetails.getCeoId(), id);
        productOptionCommandUseCase.deleteProductOption(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "옵션 순서 변경",
        description = "순서 있는 ID 배열 전체를 받아 서버가 0..N-1을 부여합니다(replace-all). 그룹의 현재 "
            + "옵션 집합과 다르면 거부됩니다(PRODUCT_ORDER_TARGET_MISMATCH).")
    @PutMapping("/v1/option-groups/{optionGroupId}/options/sort")
    public ResponseEntity<ApiResponse<Void>> changeProductOptionOrder(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long optionGroupId,
        @Valid @RequestBody ProductOptionSortRequest request
    ) {
        ProductOptionOrderChangeCommand command = request.toCommand(userDetails.getCeoId(), optionGroupId);
        productOptionCommandUseCase.changeProductOptionOrder(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
