package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.adminapi.shop.application.port.in.ShopImageChangeApproveCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopImageChangeCommandUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopImageChangeRejectCommand;

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
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopImageChangeRejectRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopImageChangeRequestSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopImageChangeRequestItemResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopImageChangeQueryUseCase;

@Tag(name = "Shop Image Change Admin", description = "가게 이미지 변경 요청 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopImageChangeAdminApiController {

    private final ShopImageChangeQueryUseCase shopImageChangeQueryUseCase;
    private final ShopImageChangeCommandUseCase shopImageChangeCommandUseCase;

    public ShopImageChangeAdminApiController(ShopImageChangeQueryUseCase shopImageChangeQueryUseCase, ShopImageChangeCommandUseCase shopImageChangeCommandUseCase) {
        this.shopImageChangeQueryUseCase = shopImageChangeQueryUseCase;
        this.shopImageChangeCommandUseCase = shopImageChangeCommandUseCase;
    }

    @Operation(summary = "이미지 변경 요청 목록 조회", description = "가게 상표/대표이미지 변경 요청 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1/image-change-requests")
    public ResponseEntity<ApiResponse<List<ShopImageChangeRequestItemResponse>>> getImageChangeRequests(
        @Valid @ModelAttribute ShopImageChangeRequestSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopImageChangeRequestItemResponse> pageResponse = shopImageChangeQueryUseCase.getImageChangeRequests(
            search.status(), search.imageType(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "이미지 변경 요청 승인", description = "가게 상표/대표이미지 변경 요청을 승인하고 가게 이미지를 갱신합니다.")
    @PatchMapping("/v1/image-change-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveImageChange(@PathVariable Long requestId) {
        ShopImageChangeApproveCommand command = ShopImageChangeApproveCommand.of(requestId);
        shopImageChangeCommandUseCase.approveImageChange(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "이미지 변경 요청 반려", description = "가게 상표/대표이미지 변경 요청을 반려합니다.")
    @PatchMapping("/v1/image-change-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectImageChange(
        @PathVariable Long requestId,
        @Valid @RequestBody ShopImageChangeRejectRequest request
    ) {
        ShopImageChangeRejectCommand command = request.toCommand(requestId);
        shopImageChangeCommandUseCase.rejectImageChange(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
