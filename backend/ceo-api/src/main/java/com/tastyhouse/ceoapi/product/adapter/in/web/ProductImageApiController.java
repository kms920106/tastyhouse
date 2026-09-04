package com.tastyhouse.ceoapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductImageSortRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductImageStatusResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductImageChangeRequestCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductImageCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductImageReorderCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductImageQueryUseCase;

/**
 * 점주 메뉴 이미지 관리 API.
 *
 * <p>이미지 <b>등록</b>만 관리자 검수를 거치고, <b>순서 변경·삭제는 즉시 반영</b>된다 — 검수 대상은
 * 새 이미지의 내용이지 배치가 아니다.
 *
 * <p>모든 핸들러가 query 또는 body의 {@code shopId}로 소유권을 검증한다. 특히 이미지 삭제는 경로에
 * 메뉴·가게 식별자가 없어 서비스가 이미지 → 메뉴 → 가게로 역조회해 대조한다 — 이 저장소는 그 역조회를
 * 생략했다가 IDOR 사고를 낸 전례가 있다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Product Image", description = "점주 메뉴 이미지 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductImageApiController {

    private final ProductImageQueryUseCase productImageQueryService;
    private final ProductImageCommandUseCase productImageCommandUseCase;

    public ProductImageApiController(
        ProductImageQueryUseCase productImageQueryService,
        ProductImageCommandUseCase productImageCommandUseCase
    ) {
        this.productImageQueryService = productImageQueryService;
        this.productImageCommandUseCase = productImageCommandUseCase;
    }

    @Operation(summary = "메뉴 이미지 목록 조회",
        description = "반영된 이미지 목록(정렬 순)과 검수 요청 이력을 함께 반환합니다.")
    @GetMapping("/v1/{id}/images")
    public ResponseEntity<ApiResponse<ProductImageStatusResponse>> getProductImages(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductImageStatusResponse response = ProductImageStatusResponse.from(productImageQueryService.getImageStatus( userDetails.getCeoId(), request.shopId(), id ));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 이미지 등록 요청",
        description = "JPG/PNG, 15MB 이하, 최소 1280x960 규격만 허용합니다. 관리자 승인 시 이미지 목록 맨 뒤에 "
            + "추가됩니다. 같은 메뉴에 검수 대기 중인 요청이 있으면 거부됩니다.")
    @PostMapping(value = "/v1/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> requestProductImage(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Parameter(description = "대상 가게 ID", required = true)
        @RequestParam("shopId") Long shopId,
        @Parameter(description = "메뉴 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        ProductImageChangeRequestCommand command =
            ProductImageChangeRequestCommand.of(userDetails.getCeoId(), shopId, id);
        Long requestId = productImageCommandUseCase.requestImageChange(command, file);
        return ResponseEntity.ok(ApiResponse.success(requestId));
    }

    @Operation(summary = "메뉴 이미지 순서 변경",
        description = "화면에 보이는 순서대로 이미지 ID 전체를 보냅니다(replace-all). 목록이 최신 상태와 "
            + "일치하지 않으면 거부됩니다. 승인을 거치지 않고 즉시 반영됩니다.")
    @PutMapping("/v1/{id}/images/sort")
    public ResponseEntity<ApiResponse<Void>> changeProductImageSort(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductImageSortRequest request
    ) {
        ProductImageReorderCommand command = request.toCommand(userDetails.getCeoId(), id);
        productImageCommandUseCase.reorderImages(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 이미지 삭제",
        description = "승인을 거치지 않고 즉시 삭제됩니다. 경로에 메뉴·가게 식별자가 없으므로 서버가 "
            + "이미지에서 소속 가게를 역조회해 shopId와 대조합니다.")
    @DeleteMapping("/v1/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long imageId,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductImageDeleteCommand command = request.toImageDeleteCommand(userDetails.getCeoId(), imageId);
        productImageCommandUseCase.deleteImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
