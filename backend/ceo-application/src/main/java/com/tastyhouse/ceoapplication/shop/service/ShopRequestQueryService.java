package com.tastyhouse.ceoapplication.shop.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopRequestQueryUseCase;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestImageChangeDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestSearchCondition;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestAdjustmentResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestCommentResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestDetailResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestImageChangeResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestListItemResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestReviewBlindResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestStatusResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestTypeCatalogResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopRequestTypeResponse;

/**
 * 점주용 요청처리 현황 조회 서비스(CQRS query 측).
 *
 * <p><b>조회 기간 상한을 두지 않는다.</b> 변경이력의 6개월 제한
 * ({@code SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE})을 복제하지 않으며, 이는 실수가 아니라 판단이다 —
 * 요청처리 현황은 "내가 낸 요청의 결과"라서 오래된 건도 근거로 열람해야 하고(반려 사유 확인, 재요청 시
 * 과거 제출물 참조), 배민 원문에도 기간 제한이 없다. <b>다음 세션이 변경이력과의 대칭을 이유로 6개월 제한을
 * 넣지 말 것.</b> 검증하는 것은 시작일↔종료일 관계 하나뿐이다.
 *
 * <p>{@code startDate}/{@code endDate}는 Bean Validation이 아니라 이 서비스가 판정한다 — 상·하한 관계가
 * 하나의 규칙이라 어노테이션으로 쪼개면 같은 규칙 위반인데 응답 계약이 갈린다(요청 record Javadoc 참조).
 *
 * <p><b>상세의 진실원은 원본 애그리거트다.</b> 인덱스에서 {@code requestType}/{@code sourceRequestId}만
 * 얻어 유형별 원본을 투영하고, 상태·반려 사유도 원본 값으로 응답한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopRequestQueryService implements ShopRequestQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestQueryService(
        ShopRequestQueryPort shopRequestQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestQueryPort = shopRequestQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 내 가게의 요청처리 현황 목록을 최신순으로 페이징 조회한다.
     *
     * <p>소유권 검증을 가장 먼저 수행한다 — 생략하면 남의 가게 요청 이력이 통째로 새는 IDOR가 된다.
     */
    @Override
    public PaginationResponse<ShopRequestListItemResponse> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        ShopRequestType requestTypeFilter = requestType == null ? null : ShopRequestType.from(requestType);
        ShopRequestStatus statusFilter = status == null ? null : ShopRequestStatus.from(status);

        ShopRequestSearchCondition condition = ShopRequestSearchCondition.of(
            shopId,
            requestTypeFilter,
            statusFilter,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<ShopRequestListItemResponse> pageResult =
            shopRequestQueryPort.findRequestPage(condition, pageQuery)
                .map(this::toListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 요청 상세를 조회한다. 존재하지 않거나 다른 가게의 요청이면 404다 — 403으로 응답하면 다른 가게 요청의
     * 존재 자체가 드러난다.
     */
    @Override
    public ShopRequestDetailResponse getRequestDetail(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return switch (detail.requestType()) {
            case TRADEMARK_CHANGE, THUMBNAIL_CHANGE -> toImageChangeDetailResponse(detail);
            case DELIVERY_AREA_ADJUSTMENT -> toAdjustmentDetailResponse(detail);
            case REVIEW_BLIND -> toReviewBlindDetailResponse(detail);
            case STORE_PRICE_VERIFICATION -> toStorePriceVerificationDetailResponse(detail);
        };
    }

    /**
     * 요청건 문의 스레드를 작성순으로 조회한다.
     */
    @Override
    public List<ShopRequestCommentResponse> getComments(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return shopRequestQueryPort.findComments(detail.requestId()).stream()
            .map(this::toCommentResponse)
            .toList();
    }

    /**
     * 필터 드롭다운용 요청 유형·상태 카탈로그. 가게에 종속되지 않는 정적 목록이라 소유권 검증이 없다.
     */
    @Override
    public ShopRequestTypeCatalogResponse getRequestTypes() {
        List<ShopRequestTypeResponse> requestTypes = Arrays.stream(ShopRequestType.values())
            .map(this::toRequestTypeResponse)
            .toList();
        List<ShopRequestStatusResponse> statuses = Arrays.stream(ShopRequestStatus.values())
            .map(this::toStatusResponse)
            .toList();
        return ShopRequestTypeCatalogResponse.from(requestTypes, statuses);
    }

    /**
     * 기간 필터의 상·하한 관계를 판정한다. 한쪽만 주면 그쪽만 적용되므로 검사할 것이 없다.
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
    }

    private ShopRequestDetailResponse toImageChangeDetailResponse(ShopRequestDetailResult detail) {
        ShopRequestImageChangeDetailResult source =
            shopRequestQueryPort.findImageChangeDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        ShopRequestImageChangeResponse imageChange = ShopRequestImageChangeResponse.from(
            source.imageType().name(),
            source.imageType().getDescription(),
            source.imageUrl()
        );
        ShopRequestStatus status = toRequestStatus(source.status());
        return toDetailResponse(
            detail,
            status.name(),
            status.getDescription(),
            source.rejectReason(),
            imageChange,
            null,
            null
        );
    }

    /**
     * 이미지 변경 승인상태를 통합 상태로 옮긴다. 값 이름이 그대로 대응하지만 {@code valueOf} 대신 switch로
     * 쓴다 — 어느 한쪽에 상수가 추가되면 컴파일이 깨져 매핑 누락이 드러난다.
     */
    private ShopRequestStatus toRequestStatus(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    /**
     * 리뷰 게시중단 요청 상세를 조립한다. 첨부 파일이 없는 유형이라 {@code attachmentUrl}은 항상 null이며,
     * 대신 대상 리뷰의 내용·평점을 서브 객체에 담는다.
     */
    private ShopRequestDetailResponse toReviewBlindDetailResponse(ShopRequestDetailResult detail) {
        ShopRequestReviewBlindDetailResult source =
            shopRequestQueryPort.findReviewBlindDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        ShopRequestReviewBlindResponse reviewBlind = ShopRequestReviewBlindResponse.from(
            source.reviewId(),
            source.reason().name(),
            source.reason().getDescription(),
            source.detailReason(),
            source.reviewContent(),
            source.reviewTotalRating()
        );
        ShopRequestStatus status = toRequestStatus(source.status());
        return toDetailResponse(
            detail,
            status.name(),
            status.getDescription(),
            source.rejectReason(),
            null,
            null,
            reviewBlind
        );
    }

    /**
     * 매장 가격 인증 요청 상세를 조립한다.
     *
     * <p><b>다른 유형과 달리 원본 애그리거트를 다시 읽지 않고 인덱스 값을 그대로 쓴다.</b> 인덱스는 파생
     * 읽기모델이라 진실원이 아니어서 상태·반려사유는 원본에서 가져오는 것이 이 서비스의 규칙이지만, 이
     * 유형의 원본은 <b>product 컨텍스트 소유</b>(승인이 하는 일의 본체가 {@code PRODUCT_PRICE} 갱신이다)여서
     * 상세를 투영하는 shop 조회 DAO가 없다. 인덱스 상태는 접수·전이 시점마다
     * {@code ShopRequestIndexRecorder}가 동기화하므로 목록과 같은 값이며, 화면이 이 유형에서 필요한 것은
     * 진행 상태와 반려 사유뿐이다.
     *
     * <p>유형 전용 서브 객체가 없다 — 대상 메뉴·매장가는 인증 현황 화면
     * ({@code GET /api/shops/v1/&#123;id&#125;/store-price-verifications/latest})이 담당하고, 이 상세는
     * 통합 요청처리 현황 목록의 공통 축(상태·첨부·문의 스레드)만 보여준다.
     */
    private ShopRequestDetailResponse toStorePriceVerificationDetailResponse(ShopRequestDetailResult detail) {
        return toDetailResponse(
            detail,
            detail.status().name(),
            detail.status().getDescription(),
            detail.rejectReason(),
            null,
            null,
            null
        );
    }

    private ShopRequestDetailResponse toAdjustmentDetailResponse(ShopRequestDetailResult detail) {
        ShopRequestAdjustmentDetailResult source =
            shopRequestQueryPort.findAdjustmentDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        ShopRequestAdjustmentResponse adjustment = ShopRequestAdjustmentResponse.from(
            source.counterpartShopName(),
            source.counterpartBusinessNumber(),
            source.franchiseName(),
            source.reason(),
            source.consentFileUrl()
        );
        ShopRequestStatus status = toRequestStatus(source.status());
        return toDetailResponse(
            detail,
            status.name(),
            status.getDescription(),
            source.rejectReason(),
            null,
            adjustment,
            null
        );
    }

    /**
     * 조정 신청 상태를 통합 상태로 옮긴다. {@code COMPLETED → APPROVED}는 배민 원문의 "승인(완료)"이 한
     * 상태이기 때문이며, 이 매핑은 인덱스를 쓰지 않는 상세 경로에서도 목록과 같은 라벨이 나오게 한다.
     */
    private ShopRequestStatus toRequestStatus(DeliveryAreaAdjustmentStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case IN_PROGRESS -> ShopRequestStatus.IN_PROGRESS;
            case COMPLETED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    private ShopRequestDetailResponse toDetailResponse(
        ShopRequestDetailResult detail,
        String status,
        String statusDescription,
        String rejectReason,
        ShopRequestImageChangeResponse imageChange,
        ShopRequestAdjustmentResponse deliveryAreaAdjustment,
        ShopRequestReviewBlindResponse reviewBlind
    ) {
        ShopRequestType requestType = detail.requestType();
        return ShopRequestDetailResponse.from(
            detail.requestId(),
            requestType.name(),
            requestType.getDescription(),
            detail.summary(),
            status,
            statusDescription,
            rejectReason,
            requestType.isContractAmending(),
            detail.attachmentUrl() != null,
            detail.commentCount(),
            detail.requestedAt(),
            detail.processedAt(),
            requestType.getAttachmentLabel(),
            detail.attachmentUrl(),
            imageChange,
            deliveryAreaAdjustment,
            reviewBlind
        );
    }

    private ShopRequestListItemResponse toListItemResponse(ShopRequestListItemResult result) {
        return ShopRequestListItemResponse.from(
            result.requestId(),
            result.requestType().name(),
            result.requestType().getDescription(),
            result.summary(),
            result.status().name(),
            result.status().getDescription(),
            result.rejectReason(),
            result.requestType().isContractAmending(),
            result.hasAttachment(),
            result.commentCount(),
            result.requestedAt(),
            result.processedAt()
        );
    }

    private ShopRequestCommentResponse toCommentResponse(ShopRequestCommentResult result) {
        return ShopRequestCommentResponse.from(
            result.commentId(),
            result.authorType().name(),
            result.authorType().getDescription(),
            result.content(),
            result.createdAt()
        );
    }

    private ShopRequestTypeResponse toRequestTypeResponse(ShopRequestType requestType) {
        return ShopRequestTypeResponse.from(
            requestType.name(),
            requestType.getDescription(),
            requestType.isContractAmending()
        );
    }

    private ShopRequestStatusResponse toStatusResponse(ShopRequestStatus status) {
        return ShopRequestStatusResponse.from(
            status.name(),
            status.getDescription()
        );
    }
}
