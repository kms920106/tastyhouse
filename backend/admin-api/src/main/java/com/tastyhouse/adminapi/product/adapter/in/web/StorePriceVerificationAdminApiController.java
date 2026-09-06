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
import com.tastyhouse.adminapi.product.adapter.in.web.request.StorePriceVerificationRejectRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.request.StorePriceVerificationSearchRequest;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationListItemResponse;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.in.StorePriceVerificationApproveCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationCommandUseCase;
import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationStartReviewCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationQueryUseCase;

@Tag(name = "Store Price Verification Admin", description = "매장 가격 인증 요청 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class StorePriceVerificationAdminApiController {
    private final StorePriceVerificationQueryUseCase storePriceVerificationQueryUseCase;
    private final StorePriceVerificationCommandUseCase storePriceVerificationCommandUseCase;

    public StorePriceVerificationAdminApiController(
        StorePriceVerificationQueryUseCase storePriceVerificationQueryUseCase,
        StorePriceVerificationCommandUseCase storePriceVerificationCommandUseCase
    ) {
        this.storePriceVerificationQueryUseCase = storePriceVerificationQueryUseCase;
        this.storePriceVerificationCommandUseCase = storePriceVerificationCommandUseCase;
    }

    @Operation(summary = "매장 가격 인증 요청 목록 조회",
        description = "점주가 낸 매장 가격 인증 요청을 상태로 필터해 페이징 조회합니다. "
            + "가격표 이미지가 검수 근거이므로 목록에 URL을 함께 담고, 대상 메뉴는 수만 담습니다"
            + "(메뉴별 대조는 상세 조회).")
    @GetMapping("/v1/store-price-verifications")
    public ResponseEntity<ApiResponse<List<StorePriceVerificationListItemResponse>>> getStorePriceVerifications(
        @Valid @ModelAttribute StorePriceVerificationSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<StorePriceVerificationListItemResult> pageResult =
            storePriceVerificationQueryUseCase.getVerifications(
                search.status(), pageRequest.page(), pageRequest.size()
            );
        PaginationResponse<StorePriceVerificationListItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(StorePriceVerificationListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "매장 가격 인증 요청 상세 조회",
        description = "요청 정보와 대상 메뉴 항목을 함께 반환합니다. 항목마다 현재 앱 노출가"
            + "(deliveryPrice)와 점주가 신고한 매장가(storePrice)가 나란히 담겨, 검수자가 가격표 "
            + "이미지와 대조해 판정할 수 있습니다.")
    @GetMapping("/v1/store-price-verifications/{id}")
    public ResponseEntity<ApiResponse<StorePriceVerificationDetailResponse>> getStorePriceVerification(
        @PathVariable Long id
    ) {
        StorePriceVerificationListItemResult verification = storePriceVerificationQueryUseCase.getVerification(id);
        StorePriceVerificationDetailResponse response = StorePriceVerificationDetailResponse.from(
            verification,
            storePriceVerificationQueryUseCase.getVerificationItems(id)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매장 가격 인증 요청 검수 착수",
        description = "대기 중인 요청을 검수 중(IN_PROGRESS)으로 전이해 선점합니다. 대기 상태에서만 "
            + "가능하며, 이미 종결된 요청은 되돌리지 않습니다.")
    @PatchMapping("/v1/store-price-verifications/{id}/review")
    public ResponseEntity<ApiResponse<Void>> startStorePriceVerificationReview(@PathVariable Long id) {
        StorePriceVerificationStartReviewCommand command = StorePriceVerificationStartReviewCommand.of(id);
        storePriceVerificationCommandUseCase.startReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "매장 가격 인증 요청 승인",
        description = "승인하면 요청에 담긴 매장가가 각 메뉴의 가격 행에 반영되고 가게 인증이 켜집니다. "
            + "반영되는 값은 요청 시점에 박제된 매장가이므로, 검수자가 화면에서 본 값과 항상 같습니다. "
            + "픽업가 동일 설정이 켜진 항목은 픽업가도 매장가와 같게 설정됩니다.")
    @PatchMapping("/v1/store-price-verifications/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveStorePriceVerification(@PathVariable Long id) {
        StorePriceVerificationApproveCommand command = StorePriceVerificationApproveCommand.of(id);
        storePriceVerificationCommandUseCase.approve(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "매장 가격 인증 요청 반려",
        description = "반려 사유는 필수입니다 — 점주가 무엇을 고쳐 다시 요청해야 하는지 알아야 합니다.")
    @PatchMapping("/v1/store-price-verifications/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectStorePriceVerification(
        @PathVariable Long id,
        @Valid @RequestBody StorePriceVerificationRejectRequest request
    ) {
        StorePriceVerificationRejectCommand command = request.toCommand(id);
        storePriceVerificationCommandUseCase.reject(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
