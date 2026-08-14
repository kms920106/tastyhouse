package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.domain.service.FakeReviewBlindRequestRepository;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.service.ShopRequestCancelService;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 요청 취소 단위 테스트.
 *
 * <p>봉인하는 규칙 세 가지 — (1) PENDING만 취소된다, (2) IN_PROGRESS는 409로 거부된다(가맹본부에 자료가
 * 전달된 뒤라 플랫폼이 일방 취소할 수 없다), (3) <b>취소는 원본 애그리거트의 상태를 바꾼다</b>. (3)이
 * 핵심인데, 인덱스에만 CANCELED를 두면 원본이 PENDING으로 남아 중복 차단이 재요청을 계속 막고 관리자가
 * 취소된 요청을 승인·반려할 수 있다.
 */
class ShopRequestCancelServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long OTHER_SHOP_ID = 2L;

    private FakeShopImageChangeRequestRepository imageRepository;
    private FakeAdjustmentRepository adjustmentRepository;
    private RecordingShopRequestIndexRepository indexRepository;
    private ShopRequestIndexRecorder recorder;
    private ShopRequestCancelService service;

    @BeforeEach
    void setUp() {
        imageRepository = new FakeShopImageChangeRequestRepository();
        adjustmentRepository = new FakeAdjustmentRepository();
        indexRepository = new RecordingShopRequestIndexRepository();
        recorder = new ShopRequestIndexRecorder(indexRepository);
        service = new ShopRequestCancelService(
            imageRepository,
            adjustmentRepository,
            new FakeReviewBlindRequestRepository(),
            recorder
        );
    }

    @Test
    @DisplayName("대기중인 이미지 변경요청을 취소하면 원본과 인덱스가 함께 CANCELED가 된다")
    void cancel_pendingImageChange_transitionsSourceAndIndex() {
        Long requestId = registerImageChangeRequest();

        service.cancel(requestId, SHOP_ID);

        assertThat(imageRepository.findById(1L).orElseThrow().getStatus())
            .as("원본이 PENDING으로 남으면 중복 차단이 재요청을 계속 막는다")
            .isEqualTo(ApprovalStatus.CANCELED);
        assertThat(indexRepository.require(ShopRequestType.TRADEMARK_CHANGE, 1L).getStatus())
            .isEqualTo(ShopRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("취소 후에는 같은 유형으로 다시 요청할 수 있다(원문의 '취소하고 다시 요청')")
    void cancel_reopensDuplicateBlock() {
        Long requestId = registerImageChangeRequest();
        assertThat(imageRepository.existsByShopIdAndImageTypeAndStatus(
            SHOP_ID, ShopImageType.TRADEMARK, ApprovalStatus.PENDING
        )).isTrue();

        service.cancel(requestId, SHOP_ID);

        assertThat(imageRepository.existsByShopIdAndImageTypeAndStatus(
            SHOP_ID, ShopImageType.TRADEMARK, ApprovalStatus.PENDING
        ))
            .as("PENDING 중복 차단이 풀려야 재요청이 가능하다")
            .isFalse();
    }

    @Test
    @DisplayName("대기중인 조정 신청도 같은 방식으로 취소된다")
    void cancel_pendingAdjustment_transitionsSourceAndIndex() {
        Long requestId = registerAdjustmentRequest(DeliveryAreaAdjustmentStatus.PENDING);

        service.cancel(requestId, SHOP_ID);

        assertThat(adjustmentRepository.findById(1L).orElseThrow().getStatus())
            .isEqualTo(DeliveryAreaAdjustmentStatus.CANCELED);
        assertThat(indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, 1L).getStatus())
            .isEqualTo(ShopRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("진행 중(IN_PROGRESS) 조정 신청은 409 SHOP_REQUEST_NOT_CANCELABLE로 거부된다")
    void cancel_inProgressAdjustment_throwsNotCancelable() {
        Long requestId = registerAdjustmentRequest(DeliveryAreaAdjustmentStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.cancel(requestId, SHOP_ID))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE));
    }

    @Test
    @DisplayName("이미 취소된 요청을 다시 취소할 수 없다")
    void cancel_alreadyCanceled_throwsNotCancelable() {
        Long requestId = registerImageChangeRequest();
        service.cancel(requestId, SHOP_ID);

        assertThatThrownBy(() -> service.cancel(requestId, SHOP_ID))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE));
    }

    @Test
    @DisplayName("다른 가게의 요청은 404로 막는다(존재 자체를 흘리지 않으려 403이 아니다)")
    void cancel_otherShopRequest_throwsNotFound() {
        Long requestId = registerImageChangeRequest();

        assertThatThrownBy(() -> service.cancel(requestId, OTHER_SHOP_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .satisfies(thrown -> assertThat(((ResourceNotFoundException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_NOT_FOUND));
    }

    @Test
    @DisplayName("취소된 조정 신청은 관리자가 반려할 수 없다(reject 종결 조건에 CANCELED가 있다)")
    void canceledAdjustment_cannotBeRejected() {
        Long requestId = registerAdjustmentRequest(DeliveryAreaAdjustmentStatus.PENDING);
        service.cancel(requestId, SHOP_ID);
        ShopDeliveryAreaAdjustmentRequest canceled = adjustmentRepository.findById(1L).orElseThrow();

        assertThatThrownBy(() -> canceled.reject("형식 미비"))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_CLOSED));
    }

    /**
     * 이미지 변경요청 1건과 그에 대응하는 인덱스 행을 만든다.
     *
     * @return 인덱스 행 ID(요청의 대외 식별자)
     */
    private Long registerImageChangeRequest() {
        ShopImageChangeRequest saved = imageRepository.save(ShopImageChangeRequest.of(
            ShopId.of(SHOP_ID), ShopImageType.TRADEMARK, UploadedFileId.of(4821L)
        ));
        recorder.record(
            ShopId.of(SHOP_ID),
            ShopRequestType.TRADEMARK_CHANGE,
            saved.getId(),
            "상표 변경요청(파일 #4821)",
            saved.getImageFileId(),
            7L
        );
        return indexRepository.require(ShopRequestType.TRADEMARK_CHANGE, saved.getId()).getId();
    }

    /**
     * 조정 신청 1건과 인덱스 행을 만들고, 필요하면 IN_PROGRESS까지 전이시킨다.
     *
     * @return 인덱스 행 ID
     */
    private Long registerAdjustmentRequest(DeliveryAreaAdjustmentStatus status) {
        ShopDeliveryAreaAdjustmentRequest saved = adjustmentRepository.save(
            ShopDeliveryAreaAdjustmentRequest.of(
                ShopId.of(SHOP_ID), "맛있는집 강남점", "1234567890", "BBQ",
                "역삼1동 전역이 중첩됩니다.", UploadedFileId.of(100L)
            )
        );
        recorder.record(
            ShopId.of(SHOP_ID),
            ShopRequestType.DELIVERY_AREA_ADJUSTMENT,
            saved.getId(),
            "맛있는집 강남점 (BBQ)",
            saved.getConsentFileId(),
            7L
        );
        if (status == DeliveryAreaAdjustmentStatus.IN_PROGRESS) {
            saved.startProgress();
            adjustmentRepository.save(saved);
            recorder.syncAdjustmentStatus(saved.getId(), saved.getStatus(), null);
        }
        return indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, saved.getId()).getId();
    }

    /**
     * 이미지 변경요청 write 포트 fake. 저장 시 식별자를 부여하고 <b>같은 인스턴스를 보관</b>해,
     * 취소 전이가 store의 행에 그대로 반영되게 한다(중복 차단 해제를 검증하려면 필요하다).
     */
    private static final class FakeShopImageChangeRequestRepository implements ShopImageChangeRequestRepository {

        private final Map<Long, ShopImageChangeRequest> requests = new HashMap<>();
        private long sequence = 0L;

        @Override
        public ShopImageChangeRequest save(ShopImageChangeRequest request) {
            if (request.getId() != null) {
                requests.put(request.getId(), request);
                return request;
            }

            ShopImageChangeRequest persisted = ShopImageChangeRequest.reconstitute(
                ++sequence,
                request.getShopId(),
                request.getImageType(),
                request.getImageFileId(),
                request.getStatus(),
                request.getRejectReason(),
                null,
                null
            );
            requests.put(persisted.getId(), persisted);
            return persisted;
        }

        @Override
        public Optional<ShopImageChangeRequest> findById(Long id) {
            return Optional.ofNullable(requests.get(id));
        }

        @Override
        public boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status) {
            return requests.values().stream().anyMatch(request ->
                request.getShopId().equals(ShopId.of(shopId))
                    && request.getImageType() == imageType
                    && request.getStatus() == status);
        }

        @Override
        public boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status) {
            return requests.values().stream().anyMatch(request ->
                request.getShopId().equals(ShopId.of(shopId)) && request.getStatus() == status);
        }
    }

    /** 조정 신청 write 포트 fake. 위와 같은 이유로 같은 인스턴스를 보관한다. */
    private static final class FakeAdjustmentRepository implements ShopDeliveryAreaAdjustmentRequestRepository {

        private final List<ShopDeliveryAreaAdjustmentRequest> store = new ArrayList<>();
        private long sequence = 0L;

        @Override
        public Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id) {
            return store.stream()
                .filter(request -> id.equals(request.getId()))
                .findFirst();
        }

        @Override
        public boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses) {
            return store.stream()
                .anyMatch(request -> request.getShopId().equals(shopId) && statuses.contains(request.getStatus()));
        }

        @Override
        public ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request) {
            if (request.getId() != null) {
                return request;
            }

            ShopDeliveryAreaAdjustmentRequest persisted = ShopDeliveryAreaAdjustmentRequest.reconstitute(
                ++sequence,
                request.getShopId(),
                request.getCounterpartShopName(),
                request.getCounterpartBusinessNumber(),
                request.getFranchiseName(),
                request.getReason(),
                request.getConsentFileId(),
                request.getStatus(),
                request.getRejectReason(),
                null,
                null
            );
            store.add(persisted);
            return persisted;
        }
    }
}
