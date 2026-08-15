package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;

/**
 * 점주의 요청 취소를 소유하는 도메인 서비스.
 *
 * <p>유형 분기를 여기 두는 이유는 두 가지다 — (1) 취소 가능 조건이 애그리거트 불변식이므로 판정이 도메인에
 * 있어야 하고, (2) api 모듈에 두면 CommandService가 write 포트 2개를 알게 된다.
 *
 * <p><b>취소는 원본 애그리거트의 상태 전이다.</b> 인덱스에만 CANCELED를 두면 진실원이 갈라져 원본이 여전히
 * PENDING이므로 중복 차단이 재요청을 막고(원문의 "취소하고 다시 요청"이 성립하지 않는다), 관리자가 이미
 * 취소된 요청을 승인·반려할 수 있다.
 *
 * <p><b>취소 가능 조건은 PENDING만이다.</b> {@code IN_PROGRESS}(배달지역 조정)는 이미 가맹본부에 자료가
 * 전달된 뒤라 플랫폼이 일방 취소하면 외부 절차와 시스템 상태가 어긋난다.
 *
 * <p>취소 주체는 점주만이다 — 관리자 종결은 "반려"(사유 필수)로 표현해 사유 없는 취소와 섞지 않는다.
 * 취소된 요청의 업로드 파일은 삭제하지 않는다(첨부 이력 보존, 상세에서 계속 열람).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다.
 */
public class ShopRequestCancelService {

    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository;
    private final ReviewBlindRequestRepository reviewBlindRequestRepository;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCancelService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestRepository = shopImageChangeRequestRepository;
        this.shopDeliveryAreaAdjustmentRequestRepository = shopDeliveryAreaAdjustmentRequestRepository;
        this.reviewBlindRequestRepository = reviewBlindRequestRepository;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    /**
     * 점주가 자기 가게의 요청을 취소한다. 원본 애그리거트 전이와 인덱스 동기화가 한 트랜잭션에서 일어난다
     * (트랜잭션 경계는 호출하는 api 모듈 CommandService가 선언한다).
     *
     * @throws com.tastyhouse.domain.exception.BusinessException 요청이 없거나 다른 가게의 요청이면
     *     {@code SHOP_REQUEST_NOT_FOUND}, PENDING이 아니면 {@code SHOP_REQUEST_NOT_CANCELABLE}
     */
    public void cancel(Long requestId, Long shopId) {
        ShopRequestIndex index = shopRequestIndexRecorder.getRequestOfShop(requestId, shopId);
        Long sourceRequestId = index.getSourceRequestId();

        switch (index.getRequestType()) {
            case TRADEMARK_CHANGE, THUMBNAIL_CHANGE -> cancelImageChange(sourceRequestId);
            case DELIVERY_AREA_ADJUSTMENT -> cancelAdjustment(sourceRequestId);
            case REVIEW_BLIND -> cancelReviewBlind(sourceRequestId);
        }

        shopRequestIndexRecorder.syncCanceled(index.getRequestType(), sourceRequestId);
    }

    private void cancelImageChange(Long sourceRequestId) {
        ShopImageChangeRequest request = shopImageChangeRequestRepository.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopImageChangeRequestRepository.save(request);
    }

    /**
     * 리뷰 게시중단 요청을 취소한다.
     *
     * <p>PENDING이 아니면 애그리거트가 {@code REVIEW_BLIND_REQUEST_NOT_PENDING}을 던지지만, 통합
     * 요청처리 화면의 취소는 유형과 무관하게 {@code SHOP_REQUEST_NOT_CANCELABLE} 하나로 응답해야 하므로
     * 여기서 번역한다 — 프론트가 유형별 에러코드 N종을 알 필요가 없어야 한다는 「요청 취소 규칙」이다.
     */
    private void cancelReviewBlind(Long sourceRequestId) {
        ReviewBlindRequest request = reviewBlindRequestRepository
            .findById(ReviewBlindRequestId.of(sourceRequestId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        request.cancel();
        reviewBlindRequestRepository.save(request);
    }

    private void cancelAdjustment(Long sourceRequestId) {
        ShopDeliveryAreaAdjustmentRequest request = shopDeliveryAreaAdjustmentRequestRepository.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
    }
}
