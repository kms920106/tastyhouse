package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 도메인 서비스 순수 단위 테스트. fake write 포트를 주입해 중복 신청 차단 불변식과
 * 상태 전이 후 <b>명시적 save</b>(POJO라 더티 체킹이 없다)를 검증한다.
 */
class ShopDeliveryAreaAdjustmentServiceTest {

    private FakeRepository repository;
    private RecordingShopChangeHistoryRepository historyRepository;
    private RecordingShopRequestIndexRepository indexRepository;
    private ShopDeliveryAreaAdjustmentService service;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        historyRepository = new RecordingShopChangeHistoryRepository();
        indexRepository = new RecordingShopRequestIndexRepository();
        service = new ShopDeliveryAreaAdjustmentService(
            repository,
            new ShopChangeHistoryRecorder(historyRepository),
            new ShopRequestIndexRecorder(indexRepository)
        );
    }

    @Test
    @DisplayName("접수는 상대 가게명·가맹본부명으로 CREATE 이력 1행을 남긴다")
    void request_recordsCreateHistory() {
        request();

        List<ShopChangeHistory> histories =
            historyRepository.savedOf(ShopChangeType.DELIVERY_AREA_ADJUSTMENT);
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.CREATE);
        assertThat(histories.getFirst().getPreviousValue()).isNull();
        assertThat(histories.getFirst().getNewValue()).isEqualTo("맛있는집 강남점 (맛있는집 본사)");
    }

    @Test
    @DisplayName("이후 상태 전이는 가게 설정 변경이 아니므로 변경이력을 남기지 않는다")
    void statusTransitions_recordNoHistory() {
        Long requestId = request();
        int afterRequest = historyRepository.saved().size();

        service.startProgress(requestId);
        service.complete(requestId);

        assertThat(historyRepository.saved()).hasSize(afterRequest);
    }

    @Test
    @DisplayName("진행 중 신청이 없으면 접수되고 생성된 식별자를 반환한다")
    void request_withoutOpenRequest_savesAndReturnsId() {
        Long requestId = request();

        assertThat(requestId).isNotNull();
        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.PENDING);
    }

    @Test
    @DisplayName("같은 가게에 진행 중(PENDING·IN_PROGRESS) 신청이 있으면 접수를 거절한다")
    void request_withOpenRequest_throws() {
        request();

        assertThatThrownBy(this::request)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("종결된 신청만 있으면 다시 접수할 수 있다")
    void request_afterClosedRequest_succeeds() {
        Long requestId = request();
        service.reject(requestId, "형식 미비");

        assertThat(request()).isNotNull();
    }

    @Test
    @DisplayName("startProgress는 애그리거트 전이 후 명시적으로 저장한다")
    void startProgress_savesTransitionedRequest() {
        Long requestId = request();
        repository.saved.clear();

        service.startProgress(requestId);

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("complete는 애그리거트 전이 후 명시적으로 저장한다")
    void complete_savesTransitionedRequest() {
        Long requestId = request();
        service.startProgress(requestId);
        repository.saved.clear();

        service.complete(requestId);

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("reject는 사유와 함께 저장한다")
    void reject_savesReasonWithRejectedStatus() {
        Long requestId = request();
        repository.saved.clear();

        service.reject(requestId, "동의서가 식별되지 않습니다.");

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.REJECTED);
        assertThat(repository.saved.getFirst().getRejectReason()).isEqualTo("동의서가 식별되지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 신청을 처리하면 실패한다")
    void startProgress_withUnknownId_throws() {
        assertThatThrownBy(() -> service.startProgress(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("접수는 요청 인덱스 1행을 PENDING으로 만든다")
    void request_createsRequestIndexRow() {
        Long requestId = request();

        ShopRequestIndex index =
            indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, requestId);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.PENDING);
        assertThat(index.getSummary()).isEqualTo("맛있는집 강남점 (맛있는집 본사)");
        assertThat(index.getRequestedByCeoId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("startProgress는 인덱스를 IN_PROGRESS로 동기화한다")
    void startProgress_syncsRequestIndex() {
        Long requestId = request();

        service.startProgress(requestId);

        assertThat(indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, requestId).getStatus())
            .isEqualTo(ShopRequestStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("complete는 인덱스를 APPROVED로 동기화한다(완료와 승인은 점주 화면에서 한 상태다)")
    void complete_syncsRequestIndexAsApproved() {
        Long requestId = request();
        service.startProgress(requestId);

        service.complete(requestId);

        assertThat(indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, requestId).getStatus())
            .isEqualTo(ShopRequestStatus.APPROVED);
    }

    @Test
    @DisplayName("reject는 인덱스를 REJECTED로 동기화하고 사유를 함께 남긴다")
    void reject_syncsRequestIndexWithReason() {
        Long requestId = request();

        service.reject(requestId, "동의서가 식별되지 않습니다.");

        ShopRequestIndex index =
            indexRepository.require(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, requestId);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.REJECTED);
        assertThat(index.getRejectReason()).isEqualTo("동의서가 식별되지 않습니다.");
        assertThat(index.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("취소된 신청은 반려할 수 없다(reject 종결 조건에 CANCELED가 포함된다)")
    void reject_afterCancel_throws() {
        Long requestId = request();
        ShopDeliveryAreaAdjustmentRequest saved = repository.findById(requestId).orElseThrow();
        saved.cancel();

        assertThatThrownBy(() -> service.reject(requestId, "형식 미비"))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_CLOSED));
    }

    private Long request() {
        return service.request(
            ShopId.of(1L),
            "맛있는집 강남점",
            "1234567890",
            "맛있는집 본사",
            "역삼1동 전역이 중첩됩니다.",
            UploadedFileId.of(100L),
            ShopChangeActor.ceo(9L)
        );
    }

    /**
     * 저장된 신청을 식별자로 되찾아 주는 최소 fake. 신규 저장 시 식별자를 부여해
     * {@code reconstitute}로 영속 상태를 흉내 내며, {@code saved}에 저장 호출을 기록해
     * 명시적 save 여부를 검증할 수 있게 한다.
     */
    private static final class FakeRepository implements ShopDeliveryAreaAdjustmentRequestRepository {

        private final List<ShopDeliveryAreaAdjustmentRequest> store = new ArrayList<>();
        private final List<ShopDeliveryAreaAdjustmentRequest> saved = new ArrayList<>();
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
            saved.add(request);

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
