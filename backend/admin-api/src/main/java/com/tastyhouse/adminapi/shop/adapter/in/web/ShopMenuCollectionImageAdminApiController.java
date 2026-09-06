package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectCommand;

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
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopMenuCollectionImageRejectRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopMenuCollectionImageSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopMenuCollectionImageRequestItemResponse;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.domain.shared.page.PageResult;

@Tag(name = "Shop Menu Collection Image Admin", description = "메뉴모음컷 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopMenuCollectionImageAdminApiController {
    private final ShopMenuCollectionImageManagementQueryUseCase shopMenuCollectionImageQueryUseCase;
    private final ShopMenuCollectionImageManagementCommandUseCase shopMenuCollectionImageCommandUseCase;

    public ShopMenuCollectionImageAdminApiController(
        ShopMenuCollectionImageManagementQueryUseCase shopMenuCollectionImageQueryUseCase,
        ShopMenuCollectionImageManagementCommandUseCase shopMenuCollectionImageCommandUseCase
    ) {
        this.shopMenuCollectionImageQueryUseCase = shopMenuCollectionImageQueryUseCase;
        this.shopMenuCollectionImageCommandUseCase = shopMenuCollectionImageCommandUseCase;
    }

    @Operation(summary = "메뉴모음컷 검수 목록 조회",
        description = "점주가 올린 메뉴모음컷을 승인 상태로 필터해 페이징 조회합니다. 가게명과 표시용 이미지 "
            + "URL을 함께 반환합니다.")
    @GetMapping("/v1/menu-collection-images/requests")
    public ResponseEntity<ApiResponse<List<ShopMenuCollectionImageRequestItemResponse>>> getMenuCollectionImageRequests(
        @Valid @ModelAttribute ShopMenuCollectionImageSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ShopMenuCollectionImageRequestResult> pageResult =
            shopMenuCollectionImageQueryUseCase.getMenuCollectionImageRequests(
                search.status(), pageRequest.page(), pageRequest.size()
            );
        PaginationResponse<ShopMenuCollectionImageRequestItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(ShopMenuCollectionImageRequestItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "메뉴모음컷 승인",
        description = "승인하면 그 즉시 손님 화면 최상단에 노출됩니다. 대기 상태가 아닌 건은 처리할 수 없습니다.")
    @PatchMapping("/v1/menu-collection-images/requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveMenuCollectionImage(@PathVariable Long id) {
        ShopMenuCollectionImageApproveCommand command = ShopMenuCollectionImageApproveCommand.of(id);
        shopMenuCollectionImageCommandUseCase.approveMenuCollectionImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴모음컷 반려", description = "반려 사유는 필수입니다.")
    @PatchMapping("/v1/menu-collection-images/requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectMenuCollectionImage(
        @PathVariable Long id,
        @Valid @RequestBody ShopMenuCollectionImageRejectRequest request
    ) {
        ShopMenuCollectionImageRejectCommand command = request.toCommand(id);
        shopMenuCollectionImageCommandUseCase.rejectMenuCollectionImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
