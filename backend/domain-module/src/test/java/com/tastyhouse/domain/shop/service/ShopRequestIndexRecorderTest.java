package com.tastyhouse.domain.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 요청 인덱스 동기화 단위 테스트.
 *
 * <p>원본 → 통합 상태 <b>매핑 표를 전수</b> 봉인한다. 특히 조정 신청의
 * {@code COMPLETED → APPROVED}는 유일하게 값 이름이 어긋나는 매핑이라, 여기서 고정하지 않으면
 * 목록에 "완료"라는 없는 상태가 새어 나가거나 매핑이 조용히 뒤집힌다.
 */
class ShopRequestIndexRecorderTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final Long SOURCE_ID = 500L;

    private RecordingShopRequestIndexRepository repository;
    private ShopRequestIndexRecorder recorder;

    @BeforeEach
    void setUp() {
        repository = new RecordingShopRequestIndexRepository();
        recorder = new ShopRequestIndexRecorder(repository);
    }

    @Test
    @DisplayName("접수 기록은 PENDING 상태로 인덱스 1행을 만든다")
    void record_createsPendingRow() {
        recordTrademarkRequest();

        ShopRequestIndex index = repository.require(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.PENDING);
        assertThat(index.getSummary()).isEqualTo("상표 변경요청(파일 #4821)");
        assertThat(index.getRequestedByCeoId()).isEqualTo(7L);
        assertThat(index.getProcessedAt()).isNull();
        assertThat(index.getRejectReason()).isNull();
    }

    @Test
    @DisplayName("이미지 변경 상태 매핑 — PENDING·APPROVED·REJECTED·CANCELED가 동일 이름으로 대응한다")
    void syncImageChangeStatus_mapsEveryApprovalStatus() {
        assertImageChangeMapping(ApprovalStatus.PENDING, ShopRequestStatus.PENDING);
        assertImageChangeMapping(ApprovalStatus.APPROVED, ShopRequestStatus.APPROVED);
        assertImageChangeMapping(ApprovalStatus.REJECTED, ShopRequestStatus.REJECTED);
        assertImageChangeMapping(ApprovalStatus.CANCELED, ShopRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("조정 신청 상태 매핑 — COMPLETED는 APPROVED로 접힌다(배민 원문의 '승인(완료)')")
    void syncAdjustmentStatus_mapsCompletedToApproved() {
        assertAdjustmentMapping(DeliveryAreaAdjustmentStatus.COMPLETED, ShopRequestStatus.APPROVED);
    }

    @Test
    @DisplayName("조정 신청 상태 매핑 — 나머지 4종은 동일 이름으로 대응한다")
    void syncAdjustmentStatus_mapsRemainingStatuses() {
        assertAdjustmentMapping(DeliveryAreaAdjustmentStatus.PENDING, ShopRequestStatus.PENDING);
        assertAdjustmentMapping(DeliveryAreaAdjustmentStatus.IN_PROGRESS, ShopRequestStatus.IN_PROGRESS);
        assertAdjustmentMapping(DeliveryAreaAdjustmentStatus.REJECTED, ShopRequestStatus.REJECTED);
        assertAdjustmentMapping(DeliveryAreaAdjustmentStatus.CANCELED, ShopRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("게시중단 동기화는 넘겨받은 통합 상태를 그대로 기록한다(매핑은 review 쪽 소유)")
    void syncBlindRequestStatus_storesGivenStatus() {
        assertBlindRequestMapping(ShopRequestStatus.APPROVED);
        assertBlindRequestMapping(ShopRequestStatus.REJECTED);
        assertBlindRequestMapping(ShopRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("반려 동기화는 사유와 처리 시각을 함께 남긴다")
    void syncStatus_withRejectReason_storesReasonAndProcessedAt() {
        recordTrademarkRequest();

        recorder.syncImageChangeStatus(
            ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID, ApprovalStatus.REJECTED, "해상도가 낮습니다."
        );

        ShopRequestIndex index = repository.require(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID);
        assertThat(index.getRejectReason()).isEqualTo("해상도가 낮습니다.");
        assertThat(index.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("취소 동기화는 사유를 비운다(취소는 사유 없는 종결이다)")
    void syncCanceled_clearsRejectReason() {
        recordTrademarkRequest();
        recorder.syncImageChangeStatus(
            ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID, ApprovalStatus.REJECTED, "해상도가 낮습니다."
        );

        recorder.syncCanceled(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID);

        ShopRequestIndex index = repository.require(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.CANCELED);
        assertThat(index.getRejectReason()).isNull();
    }

    @Test
    @DisplayName("인덱스 행이 없으면 SHOP_REQUEST_NOT_FOUND로 실패해 원본 트랜잭션을 롤백시킨다")
    void syncStatus_withoutIndexRow_throwsToRollbackSourceTransaction() {
        assertThatThrownBy(() -> recorder.syncAdjustmentStatus(
            999L, DeliveryAreaAdjustmentStatus.IN_PROGRESS, null
        ))
            .isInstanceOf(ResourceNotFoundException.class)
            .satisfies(thrown -> assertThat(((ResourceNotFoundException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 가게 요청은 404로 막는다(존재 자체를 흘리지 않으려 403이 아니다)")
    void getRequestOfShop_withOtherShop_throwsNotFound() {
        recordTrademarkRequest();
        Long requestId = repository.require(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID).getId();

        assertThatThrownBy(() -> recorder.getRequestOfShop(requestId, 999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .satisfies(thrown -> assertThat(((ResourceNotFoundException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_NOT_FOUND));
    }

    private void assertImageChangeMapping(ApprovalStatus source, ShopRequestStatus expected) {
        setUp();
        recordTrademarkRequest();

        recorder.syncImageChangeStatus(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID, source, null);

        assertThat(repository.require(ShopRequestType.TRADEMARK_CHANGE, SOURCE_ID).getStatus())
            .as("이미지 변경 %s는 통합 상태 %s에 대응해야 한다", source, expected)
            .isEqualTo(expected);
    }

    /**
     * 게시중단은 형제 메서드들과 달리 <b>통합 상태를 그대로 받는다</b> — 컨텍스트 경계 때문에 recorder가
     * {@code review.model.ReviewBlindStatus}를 import할 수 없어, 원본 상태 → 통합 상태 매핑은
     * {@code ReviewBlindRequestService}가 소유한다. 그 매핑 표(특히 {@code EXPIRED}/{@code DELETED} →
     * {@code APPROVED})는 {@code ReviewBlindRequestServiceTest}가 봉인한다.
     */
    private void assertBlindRequestMapping(ShopRequestStatus status) {
        setUp();
        recordBlindRequest();

        recorder.syncBlindRequestStatus(SOURCE_ID, status, null);

        assertThat(repository.require(ShopRequestType.REVIEW_BLIND, SOURCE_ID).getStatus())
            .as("게시중단 동기화는 넘겨받은 %s를 그대로 기록해야 한다", status)
            .isEqualTo(status);
    }

    private void recordBlindRequest() {
        recorder.record(
            SHOP_ID,
            ShopRequestType.REVIEW_BLIND,
            SOURCE_ID,
            "리뷰 게시중단 요청 - 욕설·비방(리뷰 #482)",
            null,
            7L
        );
    }

    private void assertAdjustmentMapping(DeliveryAreaAdjustmentStatus source, ShopRequestStatus expected) {
        setUp();
        recordAdjustmentRequest();

        recorder.syncAdjustmentStatus(SOURCE_ID, source, null);

        assertThat(repository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, SOURCE_ID).getStatus())
            .as("조정 신청 %s는 통합 상태 %s에 대응해야 한다", source, expected)
            .isEqualTo(expected);
    }

    private void recordTrademarkRequest() {
        recorder.record(
            SHOP_ID,
            ShopRequestType.TRADEMARK_CHANGE,
            SOURCE_ID,
            "상표 변경요청(파일 #4821)",
            UploadedFileId.of(4821L),
            7L
        );
    }

    private void recordAdjustmentRequest() {
        recorder.record(
            SHOP_ID,
            ShopRequestType.DELIVERY_AREA_ADJUSTMENT,
            SOURCE_ID,
            "맛있는집 강남점 (BBQ)",
            UploadedFileId.of(100L),
            7L
        );
    }
}
