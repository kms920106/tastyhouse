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
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupDeleteRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupSearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupUpdateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupOwnerCreateCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupDeleteCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupUpdateCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupQueryUseCase;

/**
 * 점주 옵션그룹 관리 API.
 *
 * <p>옵션그룹은 <b>여러 메뉴에 연결될 수 있으므로 가게 단위 리소스</b>다 — 그래서 목록·등록이 메뉴
 * 하위 경로가 아니라 {@code /option-groups}에 있다. 어느 메뉴에 연결하느냐는 별도 관심사이며
 * {@link ProductOptionGroupLinkApiController}가 소유한다.
 *
 * <p>{@code shopId}는 경로가 아니라 query 또는 body로 받아 소유권을 검증한다. 삭제도 body로 받는다 —
 * 메뉴 일괄 삭제({@link ProductApiController#deleteProducts})와 동일한 컨벤션이며, 프론트엔드
 * {@code ApiClient#delete}가 DELETE 요청 본문에 JSON으로 {@code shopId}를 담아 보낸다.
 *
 * <p>경로 식별자가 옵션그룹인 엔드포인트는 가게 소유권만으로는 부족하다 — 그룹의 소유 가게를
 * 역조회해 대조한다({@code ProductOptionGroupOwnershipValidator}).
 */
@Tag(name = "Ceo Product Option Group", description = "점주 옵션그룹 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductOptionGroupApiController {

    private final ProductOptionGroupQueryUseCase productOptionGroupQueryService;
    private final ProductOptionGroupCommandUseCase productOptionGroupCommandUseCase;

    public ProductOptionGroupApiController(
        ProductOptionGroupQueryUseCase productOptionGroupQueryService,
        ProductOptionGroupCommandUseCase productOptionGroupCommandUseCase
    ) {
        this.productOptionGroupQueryService = productOptionGroupQueryService;
        this.productOptionGroupCommandUseCase = productOptionGroupCommandUseCase;
    }

    @Operation(summary = "옵션그룹 목록 조회",
        description = "가게의 일반 옵션그룹을 옵션 목록·연결 메뉴 수와 함께 반환합니다. 감춘 그룹·옵션도 "
            + "포함합니다.")
    @GetMapping("/v1/option-groups")
    public ResponseEntity<ApiResponse<List<ProductOptionGroupResponse>>> getProductOptionGroups(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductOptionGroupSearchRequest request
    ) {
        List<ProductOptionGroupResponse> response = productOptionGroupQueryService.getProductOptionGroups( userDetails.getCeoId(), request.shopId() ).stream()
            .map(ProductOptionGroupResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션그룹 추가",
        description = "생성된 옵션그룹 ID만 반환합니다. 지정한 메뉴에 곧바로 연결되며, 연결 순서는 서버가 "
            + "그 메뉴의 맨 뒤로 채웁니다.")
    @PostMapping("/v1/option-groups")
    public ResponseEntity<ApiResponse<Long>> createProductOptionGroup(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductOptionGroupCreateRequest request
    ) {
        ProductOptionGroupOwnerCreateCommand command = request.toCommand(userDetails.getCeoId());
        Long optionGroupId = productOptionGroupCommandUseCase.createProductOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(optionGroupId));
    }

    @Operation(summary = "옵션그룹명·선택 제약 변경",
        description = "최소 선택 개수가 최대 선택 개수보다 크면 거부됩니다"
            + "(PRODUCT_OPTION_GROUP_SELECT_RANGE_INVALID). 연결과 순서는 이 경로로 바꾸지 않습니다. "
            + "옵션그룹 유형(groupType)은 받지 않습니다 — 유형 전환은 과거 주문 스냅샷의 해석을 소급 "
            + "변경하므로 경로 자체를 두지 않습니다.")
    @PutMapping("/v1/option-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProductOptionGroup(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupUpdateRequest request
    ) {
        ProductOptionGroupUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        productOptionGroupCommandUseCase.updateProductOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "옵션그룹 삭제",
        description = "행을 지우지 않고 감춥니다(소프트 삭제) — 과거 주문에 박제된 옵션 이력을 보존하기 "
            + "위함입니다. 메뉴판과 손님 화면에서는 즉시 사라집니다.")
    @DeleteMapping("/v1/option-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductOptionGroup(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupDeleteRequest request
    ) {
        ProductOptionGroupDeleteCommand command = request.toCommand(userDetails.getCeoId(), id);
        productOptionGroupCommandUseCase.deleteProductOptionGroup(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
