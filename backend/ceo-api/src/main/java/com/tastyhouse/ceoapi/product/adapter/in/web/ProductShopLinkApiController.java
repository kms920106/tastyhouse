package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopLinkCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopLinkReplaceRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductShopLinkResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkCreateCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkDeleteCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkReplaceCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkQueryUseCase;

/**
 * 점주 메뉴-가게 연결 관리 API — 하나의 메뉴를 여러 가게 메뉴판에 노출한다.
 *
 * <p><b>메뉴가 삭제되는 것이 아니라 노출 범위만 바뀐다.</b> 연결을 해제해도 메뉴 자체와 그 메뉴의
 * 가격·옵션·리뷰는 그대로 남고, 그 가게 메뉴판에서만 사라진다.
 *
 * <p><b>진입 축이 두 개다.</b> {@code PUT /v1/{id}/shops}는 <b>메뉴 기준</b>(이 메뉴를 어느 가게들에
 * 노출할지 한 번에 정한다)이고, {@code POST·DELETE /v1/{id}/shops/{targetShopId}}는 <b>가게 기준</b>
 * (이 가게 메뉴판에 메뉴를 불러오거나 뺀다)이다. 화면 진입 경로가 달라 둘 다 필요하다.
 *
 * <p>가격은 연결된 가게끼리 <b>공유</b>된다 — 가게별로 다른 가격이 필요하면 메뉴를 따로 만든다.
 * 옵션그룹은 원본 소유 가게가 계속 소유하며 연결된 가게는 읽기만 한다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Product Shop Link", description = "점주 메뉴-가게 연결 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductShopLinkApiController {

    private final ProductShopLinkQueryUseCase productShopLinkQueryService;
    private final ProductShopLinkCommandUseCase productShopLinkCommandUseCase;

    public ProductShopLinkApiController(
        ProductShopLinkQueryUseCase productShopLinkQueryService,
        ProductShopLinkCommandUseCase productShopLinkCommandUseCase
    ) {
        this.productShopLinkQueryService = productShopLinkQueryService;
        this.productShopLinkCommandUseCase = productShopLinkCommandUseCase;
    }

    @Operation(summary = "연결된 가게 목록 조회",
        description = "점주가 소유한 전체 가게와 각각에 대한 이 메뉴의 연결 여부(linked)를 조회합니다. "
            + "연결되지 않은 가게도 담기므로 화면이 토글로 켤 수 있습니다. 연결되지 않은 가게의 "
            + "productCategoryId·productCategoryName은 null입니다.")
    @GetMapping("/v1/{id}/shops")
    public ResponseEntity<ApiResponse<List<ProductShopLinkResponse>>> getShopLinks(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Parameter(description = "메뉴 ID", example = "100") @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        List<ProductShopLinkResponse> response = productShopLinkQueryService.getShopLinks( userDetails.getCeoId(), request.shopId(), id ).stream()
            .map(ProductShopLinkResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴-가게 연결 변경",
        description = "전체 교체입니다 — 요청 links에 없는 가게는 연결 해제됩니다. 본인 소유 가게에만 "
            + "연결할 수 있고, 메뉴그룹은 그 가게의 것이어야 하며 필수입니다. 링크는 최소 1개 유지해야 "
            + "하고, 해제로 그 가게 메뉴판의 노출 메뉴가 0개가 되면 거절됩니다. 기존 연결의 표시 순서는 "
            + "유지되고 새 연결은 대상 가게 메뉴판 끝에 붙습니다.")
    @PutMapping("/v1/{id}/shops")
    public ResponseEntity<ApiResponse<Void>> replaceShopLinks(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Parameter(description = "메뉴 ID", example = "100") @PathVariable Long id,
        @Valid @RequestBody ProductShopLinkReplaceRequest request
    ) {
        ProductShopLinkReplaceCommand command = request.toCommand(userDetails.getCeoId(), id);
        productShopLinkCommandUseCase.replaceLinks(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 불러오기 (가게 기준)",
        description = "대상 가게 메뉴판으로 이 메뉴를 불러옵니다. 대상 가게에 대한 소유권이 필요하며, "
            + "메뉴그룹은 그 가게의 것이어야 합니다. 이미 연결된 가게면 "
            + "PRODUCT_SHOP_LINK_ALREADY_LINKED로 거절됩니다.")
    @PostMapping("/v1/{id}/shops/{targetShopId}")
    public ResponseEntity<ApiResponse<Void>> linkToShop(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Parameter(description = "메뉴 ID", example = "100") @PathVariable Long id,
        @Parameter(description = "불러올 대상 가게 ID", example = "2") @PathVariable Long targetShopId,
        @Valid @RequestBody ProductShopLinkCreateRequest request
    ) {
        ProductShopLinkCreateCommand command = request.toCommand(userDetails.getCeoId(), id, targetShopId);
        productShopLinkCommandUseCase.linkToShop(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴판에서 제외 (가게 기준)",
        description = "대상 가게 메뉴판에서 이 메뉴를 제외합니다. 메뉴 자체는 삭제되지 않으며 다른 "
            + "가게에는 그대로 노출됩니다. 마지막 연결은 해제할 수 없고, 제외로 그 가게 메뉴판의 노출 "
            + "메뉴가 0개가 되면 거절됩니다.")
    @DeleteMapping("/v1/{id}/shops/{targetShopId}")
    public ResponseEntity<ApiResponse<Void>> unlinkFromShop(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Parameter(description = "메뉴 ID", example = "100") @PathVariable Long id,
        @Parameter(description = "제외할 대상 가게 ID", example = "2") @PathVariable Long targetShopId
    ) {
        ProductShopLinkDeleteCommand command =
            ProductShopLinkDeleteCommand.of(userDetails.getCeoId(), id, targetShopId);
        productShopLinkCommandUseCase.unlinkFromShop(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
