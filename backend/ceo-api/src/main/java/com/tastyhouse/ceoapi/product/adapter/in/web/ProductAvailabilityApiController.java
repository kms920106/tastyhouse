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
import com.tastyhouse.application.product.port.in.ProductHideCommand;
import com.tastyhouse.application.product.port.in.ProductHideUseCase;
import com.tastyhouse.application.product.port.in.ProductReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductReleaseUseCase;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerUseCase;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductAvailabilitySearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductHiddenRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductReleaseRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductSoldOutRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductSoldOutUntilRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityGroupResponse;

@Tag(name = "Ceo Product Availability", description = "점주 메뉴·옵션 품절·숨김 관리 API")
@RestController
@RequestMapping("/api/products")
class ProductAvailabilityApiController {

    private final ProductAvailabilityQueryUseCase productAvailabilityQueryUseCase;
    private final ProductSoldOutOwnerUseCase productSoldOutUseCase;
    private final ProductHideUseCase productHideUseCase;
    private final ProductReleaseUseCase productReleaseUseCase;
    private final ProductSoldOutUntilChangeUseCase productSoldOutUntilChangeUseCase;

    public ProductAvailabilityApiController(
        ProductAvailabilityQueryUseCase productAvailabilityQueryUseCase,
        ProductSoldOutOwnerUseCase productSoldOutUseCase,
        ProductHideUseCase productHideUseCase,
        ProductReleaseUseCase productReleaseUseCase,
        ProductSoldOutUntilChangeUseCase productSoldOutUntilChangeUseCase
    ) {
        this.productAvailabilityQueryUseCase = productAvailabilityQueryUseCase;
        this.productSoldOutUseCase = productSoldOutUseCase;
        this.productHideUseCase = productHideUseCase;
        this.productReleaseUseCase = productReleaseUseCase;
        this.productSoldOutUntilChangeUseCase = productSoldOutUntilChangeUseCase;
    }

    @Operation(summary = "품절·숨김 관리 메뉴 목록 조회",
        description = "메뉴그룹(카테고리) 단위로 묶어 반환합니다. 품절·숨김 항목도 포함하며 페이징이 없습니다. "
            + "품절보기·숨김보기를 함께 지정하면 OR로 동작합니다.")
    @GetMapping("/v1/availability")
    public ResponseEntity<ApiResponse<List<ProductAvailabilityGroupResponse>>> getProductAvailability(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductAvailabilitySearchRequest request
    ) {
        List<ProductAvailabilityGroupResponse> response = productAvailabilityQueryUseCase.getProductAvailability( userDetails.getCeoId(), request.shopId(), request.keyword(), request.soldOutOnly(), request.hiddenOnly() ).stream()
            .map(ProductAvailabilityGroupResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 일괄 품절",
        description = "품절 기간을 지정하지 않으면 서버가 다음 영업일 오픈 시각으로 채웁니다. "
            + "부분 실패는 200 응답의 failed에 담깁니다.")
    @PatchMapping("/v1/availability/sold-out")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> markProductsSoldOut(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductSoldOutRequest request
    ) {
        ProductSoldOutOwnerCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productSoldOutUseCase.markProductsSoldOut(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 일괄 숨김",
        description = "가게 메뉴판에 노출 메뉴 1개와 사장님 추천 메뉴 1개가 남아야 합니다. "
            + "제약에 걸린 메뉴는 failed에 담기고 나머지는 정상 적용됩니다.")
    @PatchMapping("/v1/availability/hidden")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> hideProducts(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductHiddenRequest request
    ) {
        ProductHideCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productHideUseCase.hideProducts(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 일괄 품절·숨김 해제",
        description = "ALL은 품절과 숨김을 함께 풉니다. 이미 판매중인 항목이 섞여 있어도 실패가 아닙니다(멱등).")
    @PatchMapping("/v1/availability/release")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> releaseProducts(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductReleaseRequest request
    ) {
        ProductReleaseCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productReleaseUseCase.releaseProducts(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 품절 기간 일괄 변경",
        description = "품절 상태가 아닌 대상은 failed에 담깁니다(목록을 열어둔 사이 다른 탭에서 해제됐을 수 있습니다).")
    @PatchMapping("/v1/availability/sold-out-until")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> changeProductsSoldOutUntil(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductSoldOutUntilRequest request
    ) {
        ProductSoldOutUntilChangeCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = ProductAvailabilityChangeResponse.from(productSoldOutUntilChangeUseCase.changeProductsSoldOutUntil(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
