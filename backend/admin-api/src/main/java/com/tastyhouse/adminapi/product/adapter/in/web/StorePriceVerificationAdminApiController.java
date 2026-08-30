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
import com.tastyhouse.adminapplication.product.response.StorePriceVerificationDetailResponse;
import com.tastyhouse.adminapplication.product.response.StorePriceVerificationListItemResponse;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationApproveCommand;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationCommandUseCase;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationRejectCommand;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationStartReviewCommand;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationQueryUseCase;

/**
 * 매장 가격 인증 요청 검수 관리자 API.
 *
 * <p>점주가 매장 가격표 이미지를 근거로 낸 "매장가 인증" 요청을 검수해 승인·반려한다. 승인하면 요청에
 * 담긴 매장가가 해당 메뉴들의 {@code PRODUCT_PRICE}에 반영되고 가게의 인증 표시가 켜진다.
 *
 * <p><b>경로가 {@code /api/shops}인 이유</b>는 요청이 <b>가게 단위</b>로 접수되기 때문이다(테이블명
 * {@code SHOP_STORE_PRICE_VERIFICATION}과 같은 근거). 반면 <b>자바 패키지는 {@code product}</b>다 —
 * 애그리거트·도메인 서비스·query DAO가 모두 product 컨텍스트 소유이고, 승인이 실제로 쓰는 대상이
 * {@code PRODUCT_PRICE}이기 때문이다. 경로와 패키지가 갈리는 것은 의도된 것이며, 이 분기 근거는
 * {@code StorePriceVerificationService}의 Javadoc이 상세히 설명한다.
 *
 * <p><b>검수 3단 상태를 그대로 노출한다</b>({@code PENDING} → {@code IN_PROGRESS} → 승인/반려).
 * {@code IN_PROGRESS}는 검수자가 항목을 선점했음을 뜻해, 여러 관리자가 같은 요청을 중복 검수하는 것을
 * 막고 점주 화면에 "검수 중"을 보여주기 위해 존재한다.
 *
 * <p><b>목록과 상세가 모두 필요하다.</b> 검수의 실질은 가격표 이미지 한 장과 신고된 매장가 N건을 한 줄씩
 * 맞춰 보는 <b>대조</b>이며, 요청 1건에 메뉴가 N건 달려 목록에 펼치면 페이징이 깨진다. 목록은 판정 전
 * 훑어보기(가게·상태·항목 수·가격표 이미지)를, 상세는 판정 근거(메뉴별 앱 가격 대 신고 매장가)를 담당한다.
 */
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
        PaginationResponse<StorePriceVerificationListItemResponse> pageResponse =
            storePriceVerificationQueryUseCase.getVerifications(
                search.status(), pageRequest.page(), pageRequest.size()
            );
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
        StorePriceVerificationDetailResponse response = storePriceVerificationQueryUseCase.getVerification(id);
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
