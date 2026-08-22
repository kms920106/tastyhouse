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
import com.tastyhouse.adminapi.product.response.ProductRepresentativeRequestItemResponse;
import com.tastyhouse.adminapi.product.response.ProductVegetarianRequestItemResponse;

/**
 * 메뉴 이미지·채식·사장님 추천 승인요청 검수 관리자 API.
 *
 * <p>점주가 낸 요청을 승인·반려한다. 승인 시 이미지는 그 메뉴의 이미지 목록 <b>맨 뒤</b>에 추가되고
 * (대표 이미지가 의도치 않게 바뀌지 않도록), 채식은 {@code Product.vegetarianType}에, 사장님 추천은
 * {@code Product.representative}에 반영된다.
 *
 * <p><b>승인요청 3종이 컨트롤러 하나를 공유한다.</b> 검수 유형마다 컨트롤러를 새로 만들면 관리자
 * 검수 화면이 탭마다 다른 곳을 호출해야 하고, 공통 요청·응답 계약(상태 필터·반려 사유·페이징)이
 * 유형별로 갈리기 시작한다.
 */
@Tag(name = "Product Approval Admin", description = "메뉴 이미지·채식·사장님 추천 승인요청 검수 관리자 API")
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

    @Operation(summary = "사장님 추천 메뉴 지정 요청 목록 조회",
        description = "점주가 낸 사장님 추천 지정 요청을 승인 상태로 필터해 페이징 조회합니다. "
            + "대표 메뉴는 가게 상단에 사진으로 노출되므로 메뉴 이미지가 검수 근거입니다.")
    @GetMapping("/v1/representative-requests")
    public ResponseEntity<ApiResponse<List<ProductRepresentativeRequestItemResponse>>> getRepresentativeRequests(
        @Valid @ModelAttribute ProductApprovalSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ProductRepresentativeRequestItemResponse> pageResponse =
            productApprovalQueryService.getRepresentativeRequests(
                search.status(), pageRequest.page(), pageRequest.size()
            );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "사장님 추천 메뉴 지정 요청 승인",
        description = "승인하면 해당 메뉴가 사장님 추천으로 켜집니다. 가게당 6개 제한과 이미지 요건을 "
            + "승인 시점에 다시 검증하므로, 대기 중에 상태가 달라졌으면 거부될 수 있습니다.")
    @PatchMapping("/v1/representative-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRepresentative(@PathVariable Long id) {
        productApprovalCommandService.approveRepresentative(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "사장님 추천 메뉴 지정 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/representative-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRepresentative(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        productApprovalCommandService.rejectRepresentative(id, request.rejectReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
