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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductAvailabilitySearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductHiddenRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionHiddenRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionReleaseRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionSoldOutRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionSoldOutUntilRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductReleaseRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductSoldOutRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductSoldOutUntilRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityGroupResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionAvailabilityGroupResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductHideCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductHideUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionHideCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionHideUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionReleaseCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionReleaseUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionSoldOutCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionSoldOutUntilChangeCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionSoldOutUntilChangeUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionSoldOutUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductReleaseCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductReleaseUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductSoldOutCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductSoldOutUntilChangeCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductSoldOutUntilChangeUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductSoldOutUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductAvailabilityQueryUseCase;

/**
 * 점주 메뉴·옵션 품절·숨김 관리 API.
 *
 * <p>모든 핸들러가 body 또는 query의 {@code shopId}로 소유권을 검증한다 — 일괄 API가 {@code shopId}를
 * 필수로 받게 해 "경로에 shopId가 없어 검증을 생략"하는 IDOR 형태를 구조적으로 없앤다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Product Availability", description = "점주 메뉴·옵션 품절·숨김 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductAvailabilityApiController {

    private final ProductAvailabilityQueryUseCase productAvailabilityQueryService;
    private final ProductSoldOutUseCase productSoldOutUseCase;
    private final ProductHideUseCase productHideUseCase;
    private final ProductReleaseUseCase productReleaseUseCase;
    private final ProductSoldOutUntilChangeUseCase productSoldOutUntilChangeUseCase;
    private final ProductOptionSoldOutUseCase productOptionSoldOutUseCase;
    private final ProductOptionHideUseCase productOptionHideUseCase;
    private final ProductOptionReleaseUseCase productOptionReleaseUseCase;
    private final ProductOptionSoldOutUntilChangeUseCase productOptionSoldOutUntilChangeUseCase;

    public ProductAvailabilityApiController(
        ProductAvailabilityQueryUseCase productAvailabilityQueryService,
        ProductSoldOutUseCase productSoldOutUseCase,
        ProductHideUseCase productHideUseCase,
        ProductReleaseUseCase productReleaseUseCase,
        ProductSoldOutUntilChangeUseCase productSoldOutUntilChangeUseCase,
        ProductOptionSoldOutUseCase productOptionSoldOutUseCase,
        ProductOptionHideUseCase productOptionHideUseCase,
        ProductOptionReleaseUseCase productOptionReleaseUseCase,
        ProductOptionSoldOutUntilChangeUseCase productOptionSoldOutUntilChangeUseCase
    ) {
        this.productAvailabilityQueryService = productAvailabilityQueryService;
        this.productSoldOutUseCase = productSoldOutUseCase;
        this.productHideUseCase = productHideUseCase;
        this.productReleaseUseCase = productReleaseUseCase;
        this.productSoldOutUntilChangeUseCase = productSoldOutUntilChangeUseCase;
        this.productOptionSoldOutUseCase = productOptionSoldOutUseCase;
        this.productOptionHideUseCase = productOptionHideUseCase;
        this.productOptionReleaseUseCase = productOptionReleaseUseCase;
        this.productOptionSoldOutUntilChangeUseCase = productOptionSoldOutUntilChangeUseCase;
    }

    // ── 조회 ────────────────────────────────────────────────────────────────────────

    @Operation(summary = "품절·숨김 관리 메뉴 목록 조회",
        description = "메뉴그룹(카테고리) 단위로 묶어 반환합니다. 품절·숨김 항목도 포함하며 페이징이 없습니다. "
            + "품절보기·숨김보기를 함께 지정하면 OR로 동작합니다.")
    @GetMapping("/v1/availability")
    public ResponseEntity<ApiResponse<List<ProductAvailabilityGroupResponse>>> getProductAvailability(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute ProductAvailabilitySearchRequest request
    ) {
        List<ProductAvailabilityGroupResponse> response = productAvailabilityQueryService.getProductAvailability(
            userDetails.getCeoId(), request.shopId(), request.keyword(),
            request.soldOutOnly(), request.hiddenOnly()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "품절·숨김 관리 옵션 목록 조회",
        description = "옵션그룹 단위로 묶어 반환합니다. 일반 옵션그룹과 공통 옵션그룹을 하나의 목록으로 합쳐 "
            + "내려주며, 검색어는 옵션명에 부분일치합니다.")
    @GetMapping("/v1/availability/options")
    public ResponseEntity<ApiResponse<List<ProductOptionAvailabilityGroupResponse>>> getProductOptionAvailability(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute ProductAvailabilitySearchRequest request
    ) {
        List<ProductOptionAvailabilityGroupResponse> response =
            productAvailabilityQueryService.getProductOptionAvailability(
                userDetails.getCeoId(), request.shopId(), request.keyword(),
                request.soldOutOnly(), request.hiddenOnly()
            );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── 메뉴 일괄 처리 ──────────────────────────────────────────────────────────────

    @Operation(summary = "메뉴 일괄 품절",
        description = "품절 기간을 지정하지 않으면 서버가 다음 영업일 오픈 시각으로 채웁니다. "
            + "부분 실패는 200 응답의 failed에 담깁니다.")
    @PatchMapping("/v1/availability/sold-out")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> markProductsSoldOut(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductSoldOutRequest request
    ) {
        ProductSoldOutCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productSoldOutUseCase.markProductsSoldOut(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 일괄 숨김",
        description = "가게 메뉴판에 노출 메뉴 1개와 사장님 추천 메뉴 1개가 남아야 합니다. "
            + "제약에 걸린 메뉴는 failed에 담기고 나머지는 정상 적용됩니다.")
    @PatchMapping("/v1/availability/hidden")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> hideProducts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductHiddenRequest request
    ) {
        ProductHideCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productHideUseCase.hideProducts(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 일괄 품절·숨김 해제",
        description = "ALL은 품절과 숨김을 함께 풉니다. 이미 판매중인 항목이 섞여 있어도 실패가 아닙니다(멱등).")
    @PatchMapping("/v1/availability/release")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> releaseProducts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductReleaseRequest request
    ) {
        ProductReleaseCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productReleaseUseCase.releaseProducts(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 품절 기간 일괄 변경",
        description = "품절 상태가 아닌 대상은 failed에 담깁니다(목록을 열어둔 사이 다른 탭에서 해제됐을 수 있습니다).")
    @PatchMapping("/v1/availability/sold-out-until")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> changeProductsSoldOutUntil(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductSoldOutUntilRequest request
    ) {
        ProductSoldOutUntilChangeCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productSoldOutUntilChangeUseCase.changeProductsSoldOutUntil(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── 옵션 일괄 처리 ──────────────────────────────────────────────────────────────

    @Operation(summary = "옵션 일괄 품절",
        description = "옵션그룹별로 최소 선택 개수만큼은 판매 중이어야 합니다. 제약에 걸린 옵션은 failed에 담깁니다.")
    @PatchMapping("/v1/availability/options/sold-out")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> markOptionsSoldOut(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductOptionSoldOutRequest request
    ) {
        ProductOptionSoldOutCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productOptionSoldOutUseCase.markOptionsSoldOut(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 일괄 숨김",
        description = "숨김도 선택 불가로 만들므로 품절과 동일하게 옵션그룹별 최소 선택 개수 제약이 적용됩니다.")
    @PatchMapping("/v1/availability/options/hidden")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> hideOptions(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductOptionHiddenRequest request
    ) {
        ProductOptionHideCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productOptionHideUseCase.hideOptions(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 일괄 품절·숨김 해제", description = "해제 방향에는 제약이 없습니다(멱등).")
    @PatchMapping("/v1/availability/options/release")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> releaseOptions(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductOptionReleaseRequest request
    ) {
        ProductOptionReleaseCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productOptionReleaseUseCase.releaseOptions(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션 품절 기간 일괄 변경", description = "품절 상태가 아닌 대상은 failed에 담깁니다.")
    @PatchMapping("/v1/availability/options/sold-out-until")
    public ResponseEntity<ApiResponse<ProductAvailabilityChangeResponse>> changeOptionsSoldOutUntil(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductOptionSoldOutUntilRequest request
    ) {
        ProductOptionSoldOutUntilChangeCommand command = request.toCommand(userDetails.getCeoId());
        ProductAvailabilityChangeResponse response = productOptionSoldOutUntilChangeUseCase.changeOptionsSoldOutUntil(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
