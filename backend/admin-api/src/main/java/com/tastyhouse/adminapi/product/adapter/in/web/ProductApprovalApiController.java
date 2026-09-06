package com.tastyhouse.adminapi.product.adapter.in.web;

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
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductApprovalRejectRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.ProductApprovalSearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductImageChangeRequestItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductRepresentativeRequestItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductVegetarianRequestItemResponse;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.in.ProductApprovalCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageChangeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductImageChangeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeApproveCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianApproveCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianRejectCommand;
import com.tastyhouse.application.product.port.in.ProductApprovalQueryUseCase;

@Tag(name = "Product Approval Admin", description = "메뉴 이미지·채식·사장님 추천 승인요청 검수 관리자 API")
@RestController
@RequestMapping("/api/products")
public class ProductApprovalApiController {
    private final ProductApprovalQueryUseCase productApprovalQueryUseCase;
    private final ProductApprovalCommandUseCase productApprovalCommandUseCase;

    public ProductApprovalApiController(
        ProductApprovalQueryUseCase productApprovalQueryUseCase,
        ProductApprovalCommandUseCase productApprovalCommandUseCase
    ) {
        this.productApprovalQueryUseCase = productApprovalQueryUseCase;
        this.productApprovalCommandUseCase = productApprovalCommandUseCase;
    }

    @Operation(summary = "메뉴 이미지 변경 요청 목록 조회",
        description = "점주가 낸 메뉴 이미지 등록 요청을 승인 상태로 필터해 페이징 조회합니다.")
    @GetMapping("/v1/image-change-requests")
    public ResponseEntity<ApiResponse<List<ProductImageChangeRequestItemResponse>>> getImageChangeRequests(
        @Valid @ModelAttribute ProductApprovalSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ProductImageChangeRequestResult> pageResult = productApprovalQueryUseCase.getImageChangeRequests(
            search.status(), pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<ProductImageChangeRequestItemResponse> pageResponse = PaginationResponse.from(pageResult.map(ProductImageChangeRequestItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "메뉴 이미지 변경 요청 승인",
        description = "승인하면 요청된 이미지가 그 메뉴의 이미지 목록 맨 뒤에 추가됩니다.")
    @PatchMapping("/v1/image-change-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveImageChange(@PathVariable Long id) {
        ProductImageChangeApproveCommand command = ProductImageChangeApproveCommand.of(id);
        productApprovalCommandUseCase.approveImageChange(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 이미지 변경 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/image-change-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectImageChange(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        ProductImageChangeRejectCommand command = request.toImageChangeCommand(id);
        productApprovalCommandUseCase.rejectImageChange(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 채식 설정 요청 목록 조회",
        description = "점주가 낸 채식 설정 요청을 승인 상태로 필터해 페이징 조회합니다. 포함 재료가 검수 근거입니다.")
    @GetMapping("/v1/vegetarian-requests")
    public ResponseEntity<ApiResponse<List<ProductVegetarianRequestItemResponse>>> getVegetarianRequests(
        @Valid @ModelAttribute ProductApprovalSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ProductVegetarianRequestResult> pageResult = productApprovalQueryUseCase.getVegetarianRequests(
            search.status(), pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<ProductVegetarianRequestItemResponse> pageResponse = PaginationResponse.from(pageResult.map(ProductVegetarianRequestItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "메뉴 채식 설정 요청 승인",
        description = "승인하면 요청된 채식 단계가 메뉴에 반영됩니다.")
    @PatchMapping("/v1/vegetarian-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveVegetarian(@PathVariable Long id) {
        ProductVegetarianApproveCommand command = ProductVegetarianApproveCommand.of(id);
        productApprovalCommandUseCase.approveVegetarian(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 채식 설정 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/vegetarian-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectVegetarian(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        ProductVegetarianRejectCommand command = request.toVegetarianCommand(id);
        productApprovalCommandUseCase.rejectVegetarian(command);
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
        PageResult<ProductRepresentativeRequestResult> pageResult = productApprovalQueryUseCase.getRepresentativeRequests(
            search.status(), pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<ProductRepresentativeRequestItemResponse> pageResponse = PaginationResponse.from(pageResult.map(ProductRepresentativeRequestItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "사장님 추천 메뉴 지정 요청 승인",
        description = "승인하면 해당 메뉴가 사장님 추천으로 켜집니다. 가게당 6개 제한과 이미지 요건을 "
            + "승인 시점에 다시 검증하므로, 대기 중에 상태가 달라졌으면 거부될 수 있습니다.")
    @PatchMapping("/v1/representative-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRepresentative(@PathVariable Long id) {
        ProductRepresentativeApproveCommand command = ProductRepresentativeApproveCommand.of(id);
        productApprovalCommandUseCase.approveRepresentative(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "사장님 추천 메뉴 지정 요청 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/representative-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRepresentative(
        @PathVariable Long id,
        @Valid @RequestBody ProductApprovalRejectRequest request
    ) {
        ProductRepresentativeRejectCommand command = request.toRepresentativeCommand(id);
        productApprovalCommandUseCase.rejectRepresentative(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
