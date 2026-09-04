package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

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
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupLinkRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupSortRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupLinkedProductResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupLinkedProductsResponse;
import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupQueryUseCase;

/**
 * 점주 메뉴-옵션그룹 연결 API.
 *
 * <p>연결·해제·순서를 <b>한 컨트롤러가 소유</b>한다 — 셋 모두 같은 링크 집합을 다루고, 특히 해제와
 * 순서는 남은 연결의 {@code sort}를 함께 재정규화하므로 관심사를 흩어놓으면 불변식이 두 곳으로 나뉜다.
 *
 * <p><b>핵심 불변식 — 옵션그룹은 단일 가게에만 속한다.</b> 다른 가게 메뉴에 연결하려 하면
 * {@code PRODUCT_OPTION_GROUP_SHOP_MISMATCH}(400)로 거부된다. 이 불변식 덕분에 소유권 판정에서
 * ANY/ALL 구분이 사라져 "연결된 아무 메뉴 하나"로 판정할 수 있다.
 *
 * <p><b>마지막 연결 해제는 막힌다</b>({@code PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK}) — 연결이
 * 0건이면 어디서도 보이지 않는 고아 그룹이 된다. 그룹 자체를 없애려면 옵션그룹 삭제 API를 쓴다.
 */
@Tag(name = "Ceo Product Option Group Link", description = "점주 메뉴-옵션그룹 연결 API")
@RestController
@RequestMapping("/api/products")
public class ProductOptionGroupLinkApiController {

    private final ProductOptionGroupQueryUseCase productOptionGroupQueryService;
    private final ProductOptionGroupLinkCommandUseCase productOptionGroupLinkCommandUseCase;

    public ProductOptionGroupLinkApiController(
        ProductOptionGroupQueryUseCase productOptionGroupQueryService,
        ProductOptionGroupLinkCommandUseCase productOptionGroupLinkCommandUseCase
    ) {
        this.productOptionGroupQueryService = productOptionGroupQueryService;
        this.productOptionGroupLinkCommandUseCase = productOptionGroupLinkCommandUseCase;
    }

    @Operation(summary = "메뉴에 옵션그룹 연결",
        description = "이미 연결돼 있으면 아무 일도 하지 않습니다(멱등). 다른 가게의 옵션그룹은 연결할 수 "
            + "없습니다(PRODUCT_OPTION_GROUP_SHOP_MISMATCH).")
    @PostMapping("/v1/{id}/option-groups/{optionGroupId}")
    public ResponseEntity<ApiResponse<Void>> linkOptionGroup(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long optionGroupId,
        @Valid @RequestBody ProductOptionGroupLinkRequest request
    ) {
        ProductOptionGroupLinkCommand command = request.toCommand(userDetails.getCeoId(), id, optionGroupId);
        productOptionGroupLinkCommandUseCase.linkOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴에서 옵션그룹 연결 해제",
        description = "이 그룹의 마지막 연결이면 거부됩니다"
            + "(PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK). 해제 후 남은 연결의 순서는 서버가 "
            + "0..N-1로 다시 매깁니다.")
    @DeleteMapping("/v1/{id}/option-groups/{optionGroupId}")
    public ResponseEntity<ApiResponse<Void>> unlinkOptionGroup(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long optionGroupId,
        @Valid @ModelAttribute ProductOptionGroupLinkRequest request
    ) {
        ProductOptionGroupUnlinkCommand command = request.toUnlinkCommand(userDetails.getCeoId(), id, optionGroupId);
        productOptionGroupLinkCommandUseCase.unlinkOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 내 옵션그룹 순서 변경",
        description = "순서 있는 ID 배열 전체를 받아 서버가 0..N-1을 부여합니다(replace-all). 이 메뉴에 "
            + "연결된 현재 집합과 다르면 거부됩니다(PRODUCT_ORDER_TARGET_MISMATCH).")
    @PutMapping("/v1/{id}/option-groups/sort")
    public ResponseEntity<ApiResponse<Void>> changeOptionGroupOrder(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupSortRequest request
    ) {
        ProductOptionGroupOrderChangeCommand command = request.toCommand(userDetails.getCeoId(), id);
        productOptionGroupLinkCommandUseCase.changeOptionGroupOrder(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 옵션그룹 전체의 연결 메뉴 목록 벌크 조회",
        description = "옵션그룹 연결 다이얼로그의 후보 목록 표시용입니다. 그룹마다 개별 조회하지 않도록 "
            + "가게 단위로 한 번에 반환합니다.")
    @GetMapping("/v1/option-groups/products")
    public ResponseEntity<ApiResponse<List<ProductOptionGroupLinkedProductsResponse>>> getLinkedProductsByShop(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductOptionGroupLinkRequest request
    ) {
        List<ProductOptionGroupLinkedProductsResponse> response = productOptionGroupQueryService.getLinkedProductsByShop( userDetails.getCeoId(), request.shopId() ).stream()
            .map(ProductOptionGroupLinkedProductsResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션그룹을 사용하는 메뉴 목록 조회",
        description = "연결 해제 전 영향 확인용입니다. 결과가 1건이면 마지막 연결이라 해제가 거부됩니다.")
    @GetMapping("/v1/option-groups/{optionGroupId}/products")
    public ResponseEntity<ApiResponse<List<ProductOptionGroupLinkedProductResponse>>> getLinkedProducts(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long optionGroupId,
        @Valid @ModelAttribute ProductOptionGroupLinkRequest request
    ) {
        List<ProductOptionGroupLinkedProductResponse> response = productOptionGroupQueryService.getLinkedProducts( userDetails.getCeoId(), request.shopId(), optionGroupId ).stream()
            .map(ProductOptionGroupLinkedProductResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
