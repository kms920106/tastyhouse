package com.tastyhouse.adminapi.product;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.product.request.ProductApprovalRejectRequest;
import com.tastyhouse.adminapi.product.request.ProductApprovalSearchRequest;
import com.tastyhouse.adminapi.product.response.ProductImageChangeRequestItemResponse;
import com.tastyhouse.adminapi.product.response.ProductVegetarianRequestItemResponse;

/**
 * 메뉴 이미지·채식 승인요청 검수 관리자 API.
 *
 * <p>점주가 낸 요청을 승인·반려한다. 승인 시 이미지는 그 메뉴의 이미지 목록 <b>맨 뒤</b>에 추가되고
 * (대표 이미지가 의도치 않게 바뀌지 않도록), 채식은 {@code Product.vegetarianType}에 반영된다.
 */
@Tag(name = "Product Approval Admin", description = "메뉴 이미지·채식 승인요청 검수 관리자 API")
@RestController
@RequestMapping("/api/products")
public class ProductApprovalApiController {

    private final ProductApprovalQueryService productApprovalQueryService;
    private final ProductApprovalCommandService productApprovalCommandService;

    public ProductApprovalApiController(
        ProductApprovalQueryService productApprovalQueryService,
        ProductApprovalCommandService productApprovalCommandService
    ) {
        this.productApprovalQueryService = productApprovalQueryService;
        this.productApprovalCommandService = productApprovalCommandService;
    }

    @Operation(summary = "메뉴 이미지 변경 요청 목록 조회",
        description = "점주가 낸 메뉴 이미지 등록 요청을 승인 상태로 필터해 페이징 조회합니다.")
    @GetMapping("/v1/image-change-requests")
    public ResponseEntity<ApiResponse<List<ProductImageChangeRequestItemResponse>>> getImageChangeRequests(
        @Valid @ModelAttribute ProductApprovalSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ProductImageChangeRequestItemResponse> pageResponse =
            productApprovalQueryService.getImageChangeRequests(
                search.status(), pageRequest.page(), pageRequest.size()
            );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "메뉴 이미지 변경 요청 승인",
        description = "승인하면 요청된 이미지가 그 메뉴의 이미지 목록 맨 뒤에 추가됩니다.")
    @PatchMapping("/v1/image-change-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveImageChange(@PathVariable Long id) {
        productApprovalCommandService.approveImageChange(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 이미지 변경 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/image-change-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectImageChange(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        productApprovalCommandService.rejectImageChange(id, request.rejectReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 채식 설정 요청 목록 조회",
        description = "점주가 낸 채식 설정 요청을 승인 상태로 필터해 페이징 조회합니다. 포함 재료가 검수 근거입니다.")
    @GetMapping("/v1/vegetarian-requests")
    public ResponseEntity<ApiResponse<List<ProductVegetarianRequestItemResponse>>> getVegetarianRequests(
        @Valid @ModelAttribute ProductApprovalSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ProductVegetarianRequestItemResponse> pageResponse =
            productApprovalQueryService.getVegetarianRequests(
                search.status(), pageRequest.page(), pageRequest.size()
            );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "메뉴 채식 설정 요청 승인",
        description = "승인하면 요청된 채식 단계가 메뉴에 반영됩니다.")
    @PatchMapping("/v1/vegetarian-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveVegetarian(@PathVariable Long id) {
        productApprovalCommandService.approveVegetarian(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 채식 설정 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/vegetarian-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectVegetarian(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        productApprovalCommandService.rejectVegetarian(id, request.rejectReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
