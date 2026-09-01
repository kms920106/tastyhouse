package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRequestQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopRequestCommentCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopRequestSearchRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestCommentResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestDetailResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestListItemResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestTypeCatalogResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRequestCancelCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRequestCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRequestCommentCreateCommand;

@Tag(name = "Ceo Shop Request", description = "점주 요청처리 현황 조회 API")
@RestController
@RequestMapping("/api/shops")
public class ShopRequestApiController {

    private final ShopRequestQueryUseCase shopRequestQueryService;
    private final ShopRequestCommandUseCase shopRequestCommandUseCase;

    public ShopRequestApiController(
        ShopRequestQueryUseCase shopRequestQueryService,
        ShopRequestCommandUseCase shopRequestCommandUseCase
    ) {
        this.shopRequestQueryService = shopRequestQueryService;
        this.shopRequestCommandUseCase = shopRequestCommandUseCase;
    }

    @Operation(
        summary = "요청처리 현황 목록 조회",
        description = "점주가 낸 신청의 처리 상태를 유형 구분 없이 통합해 최신순으로 조회합니다. 유형·상태·기간으로 필터할 수 있으며 조회 가능 기간에 제한이 없습니다."
    )
    @GetMapping("/v1/{id}/requests")
    public ResponseEntity<ApiResponse<List<ShopRequestListItemResponse>>> getRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopRequestSearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ShopRequestListItemViewResult> pageResult = shopRequestQueryService.getRequests(
            userDetails.getCeoId(),
            id,
            request.requestType(),
            request.status(),
            request.startDate(),
            request.endDate(),
            pageRequest.page(),
            pageRequest.size()
        );
        PaginationResponse<ShopRequestListItemResponse> response =
            PaginationResponse.from(pageResult.map(ShopRequestListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            response.content(),
            response.page(),
            response.size(),
            response.totalElements()
        ));
    }

    @Operation(
        summary = "요청처리 현황 상세 조회",
        description = "요청 1건의 상세를 조회합니다. 첨부(계약 부속서류·요청 이미지)와 유형별 신청 내용이 함께 내려갑니다. 상태·반려 사유는 원본 신청의 값입니다."
    )
    @GetMapping("/v1/{id}/requests/{requestId}")
    public ResponseEntity<ApiResponse<ShopRequestDetailResponse>> getRequestDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long requestId
    ) {
        ShopRequestDetailResponse response =
            ShopRequestDetailResponse.from(shopRequestQueryService.getRequestDetail(userDetails.getCeoId(), id, requestId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "요청 취소",
        description = "대기중인 요청을 취소합니다. 진행 중(배달지역 조정)인 요청은 이미 가맹본부에 자료가 전달된 뒤라 취소할 수 없습니다. 취소 후에는 같은 유형으로 다시 요청할 수 있습니다."
    )
    @PatchMapping("/v1/{id}/requests/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long requestId
    ) {
        ShopRequestCancelCommand command = ShopRequestCancelCommand.of(userDetails.getCeoId(), id, requestId);
        shopRequestCommandUseCase.cancelRequest(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "요청건 문의 스레드 조회",
        description = "요청 1건에 오간 문의와 담당자 답변을 작성순으로 조회합니다. 반려·취소·승인 이후에도 조회할 수 있습니다."
    )
    @GetMapping("/v1/{id}/requests/{requestId}/comments")
    public ResponseEntity<ApiResponse<List<ShopRequestCommentResponse>>> getComments(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long requestId
    ) {
        List<ShopRequestCommentResponse> response = shopRequestQueryService.getComments(userDetails.getCeoId(), id, requestId).stream()
            .map(ShopRequestCommentResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "요청건 문의 작성",
        description = "요청 1건에 문의를 남깁니다. 처리 상태와 무관하게 작성할 수 있어 반려 사유를 확인한 뒤에도 문의할 수 있습니다."
    )
    @PostMapping("/v1/{id}/requests/{requestId}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long requestId,
        @Valid @RequestBody ShopRequestCommentCreateRequest request
    ) {
        ShopRequestCommentCreateCommand command = request.toCommand(userDetails.getCeoId(), id, requestId);
        Long commentId = shopRequestCommandUseCase.addComment(command);
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }

    @Operation(
        summary = "요청 유형·상태 카탈로그 조회",
        description = "요청처리 현황 필터 드롭다운을 채우기 위한 유형·상태 목록입니다. 가게에 종속되지 않는 정적 카탈로그입니다."
    )
    @GetMapping("/v1/request-types")
    public ResponseEntity<ApiResponse<ShopRequestTypeCatalogResponse>> getRequestTypes() {
        ShopRequestTypeCatalogResponse response =
            ShopRequestTypeCatalogResponse.from(shopRequestQueryService.getRequestTypes());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
