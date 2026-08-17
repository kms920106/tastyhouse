package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 요청 인덱스 기록·동기화를 소유하는 도메인 서비스.
 *
 * <p>배선 지점을 <b>도메인 서비스로만 한정</b>한다({@link ShopImageApprovalService},
 * {@link ShopDeliveryAreaAdjustmentService}). api 모듈 CommandService에 배선하면 같은 상태 전이가 ceo/admin
 * 두 모듈에 흩어져 한쪽이 반드시 빠지고, 두 도메인 서비스는 이미 원본 애그리거트를 손에 들고 있어 추가
 * 조회가 0회다. 그래서 두 서비스는 이 Recorder를 <b>생성자 필수 의존</b>으로 받는다 — 새 전이 메서드를
 * 만들 때 컴파일 단계에서 인식하게 하려는 의도다.
 *
 * <p>도메인 이벤트·{@code AFTER_COMMIT} 리스너를 쓰지 않는다. 기록 유실이 곧 "요청이 목록에서 사라짐"이라
 * 원본 상태 전이와 같은 트랜잭션에서 동기 기록한다({@code ShopChangeHistoryRecorder}와 같은 이유).
 *
 * <p>원본 → 통합 상태 매핑은 이 클래스의 private static 메서드 2개가 소유한다. 공용
 * {@link ApprovalStatus}에 shop 요청 전용 변환 메서드를 넣으면 공용 enum이 특정 컨텍스트를 알게 되는
 * 역방향 의존이 된다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다.
 */
public class ShopRequestIndexRecorder {

    private final ShopRequestIndexRepository shopRequestIndexRepository;

    public ShopRequestIndexRecorder(ShopRequestIndexRepository shopRequestIndexRepository) {
        this.shopRequestIndexRepository = shopRequestIndexRepository;
    }

    /**
     * 요청 접수분 인덱스 행을 만든다.
     *
     * <p>{@code UNIQUE (request_type, source_request_id)}가 멱등성의 구조적 보증이다 — 배선 중복은 즉시
     * 드러나고 조용한 중복행이 생기지 않는다.
     *
     * @param requestedByCeoId 요청 점주 ID. admin이 대행 접수하는 경로가 생기면 {@code null}일 수 있다
     */
    public void record(
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId
    ) {
        shopRequestIndexRepository.save(ShopRequestIndex.of(
            shopId,
            requestType,
            sourceRequestId,
            summary,
            attachmentFileId,
            requestedByCeoId
        ));
    }

    /**
     * 이미지 변경요청의 상태 전이를 인덱스에 반영한다.
     */
    public void syncImageChangeStatus(
        ShopRequestType requestType,
        Long sourceRequestId,
        ApprovalStatus status,
        String rejectReason
    ) {
        syncStatus(requestType, sourceRequestId, toRequestStatus(status), rejectReason);
    }

    /**
     * 배달지역 조정 신청의 상태 전이를 인덱스에 반영한다.
     */
    public void syncAdjustmentStatus(
        Long sourceRequestId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        syncStatus(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, sourceRequestId, toRequestStatus(status), rejectReason);
    }

    /**
     * 리뷰 게시중단 요청의 상태 전이를 인덱스에 반영한다.
     *
     * <p>요청 유형이 {@link ShopRequestType#REVIEW_BLIND} 하나로 고정이라 유형을 인자로 받지 않는다
     * (이미지 변경이 상표·대표이미지 2종으로 갈리는 것과 다른 점).
     *
     * <p><b>review 도메인의 상태 enum이 아니라 통합 상태 {@link ShopRequestStatus}를 받는다</b> —
     * 형제 메서드들과 다른 점이며, 이유는 컨텍스트 경계다. shop 컨텍스트가
     * {@code review.model.ReviewBlindStatus}를 import하면 {@code ContextBoundaryTest}의
     * "타 컨텍스트의 model을 import하지 않는다" 규칙을 위반한다(이미지 변경·조정 신청의 상태 enum은
     * 둘 다 shop 컨텍스트 소유라 이 문제가 없다). 매핑은 그 enum을 소유한 review 쪽이 수행한다.
     */
    public void syncBlindRequestStatus(Long sourceRequestId, ShopRequestStatus status, String rejectReason) {
        syncStatus(ShopRequestType.REVIEW_BLIND, sourceRequestId, status, rejectReason);
    }

    /**
     * 취소를 인덱스에 반영한다. 취소는 사유 없는 종결이므로 {@code rejectReason}을 비운다.
     */
    public void syncCanceled(ShopRequestType requestType, Long sourceRequestId) {
        syncStatus(requestType, sourceRequestId, ShopRequestStatus.CANCELED, null);
    }

    /**
     * 인덱스 행을 찾아 상태를 동기화한다.
     *
     * <p>행을 찾지 못하면 <b>{@code SHOP_REQUEST_NOT_FOUND}로 실패시켜 원본 트랜잭션을 롤백</b>한다.
     * 조용히 무시하면 원본만 전이돼 목록이 영구히 어긋난다(백필 누락이 이 경로로 드러난다).
     */
    private void syncStatus(
        ShopRequestType requestType,
        Long sourceRequestId,
        ShopRequestStatus status,
        String rejectReason
    ) {
        ShopRequestIndex index = shopRequestIndexRepository
            .findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        index.syncStatus(status, rejectReason, LocalDateTime.now());
        shopRequestIndexRepository.save(index);
    }

    /**
     * 이미지 변경 승인상태 → 통합 상태. 값 이름이 그대로 대응한다.
     */
    private static ShopRequestStatus toRequestStatus(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    /**
     * 조정 신청 상태 → 통합 상태.
     *
     * <p><b>{@code COMPLETED}는 {@code APPROVED}로 접는다</b> — 배민 원문의 "승인(완료)"이 한 상태이고,
     * 점주 화면에서 완료와 승인을 구분할 근거가 없다.
     */
    private static ShopRequestStatus toRequestStatus(DeliveryAreaAdjustmentStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case IN_PROGRESS -> ShopRequestStatus.IN_PROGRESS;
            case COMPLETED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    /**
     * 인덱스 행을 상태 전이 경로에서 로드한다(취소·댓글 작성의 선행 조회).
     */
    public ShopRequestIndex getRequest(Long requestId) {
        return shopRequestIndexRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
    }

    /**
     * 존재하지 않는 요청·다른 가게의 요청을 모두 404로 막는다.
     *
     * <p>불일치를 403이 아니라 404로 응답하는 것은 의도적이다 — 다른 가게 요청의 <b>존재 자체</b>를 흘리지
     * 않는다.
     */
    public ShopRequestIndex getRequestOfShop(Long requestId, Long shopId) {
        ShopRequestIndex index = getRequest(requestId);
        if (!index.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND);
        }
        return index;
    }
}
