package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 이미지 변경요청 변경이력 기록 회귀 테스트.
 *
 * <p>이력 기록이 조용히 빠지는 결함(저장은 되는데 기록이 없는 부류)을 막는 것이 이 테스트의 목적이다.
 * 요청 1건당 이력 1행이며, 관리자 검수(승인·반려)는 이력을 남기지 않아야 한다.
 */
class ShopImageApprovalServiceTest {

    private static final Long SHOP_ID = 1L;

    private RecordingShopChangeHistoryRepository shopChangeHistoryRepository;
    private RecordingShopRequestIndexRepository shopRequestIndexRepository;
    private ShopImageApprovalService shopImageApprovalService;

    /** 이미지 변경요청 write 포트 fake. 저장 시 식별자를 부여해 승인 경로도 태울 수 있게 한다. */
    private static final class FakeShopImageChangeRequestRepository implements ShopImageChangeRequestRepository {

        private final Map<Long, ShopImageChangeRequest> requests = new HashMap<>();
        private long sequence = 0L;

        @Override
        public ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest) {
            Long id = shopImageChangeRequest.getId() == null ? ++sequence : shopImageChangeRequest.getId();
            ShopImageChangeRequest saved = ShopImageChangeRequest.reconstitute(
                id,
                shopImageChangeRequest.getShopId(),
                shopImageChangeRequest.getImageType(),
                shopImageChangeRequest.getImageFileId(),
                shopImageChangeRequest.getStatus(),
                shopImageChangeRequest.getRejectReason(),
                null,
                null
            );
            requests.put(id, saved);
            return saved;
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

    /** 가게 write 포트 fake. 승인 시 이미지 반영 경로가 가게를 찾을 수 있어야 한다. */
    private static final class FakeShopRepository implements ShopRepository {

        private final Map<Long, Shop> shops = new HashMap<>();

        FakeShopRepository() {
            shops.put(SHOP_ID, Shop.reconstitute(
                SHOP_ID, null, null, "맛있는 분식",
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, false, false, false, 10000, false, null, null
            ));
        }

        @Override
        public Optional<Shop> findById(ShopId shopId) {
            return Optional.ofNullable(shops.get(shopId.value()));
        }

        @Override
        public Optional<Shop> findVisibleById(ShopId shopId) {
            return findById(shopId);
        }

        @Override
        public Shop save(Shop shop) {
            shops.put(shop.getShopId().value(), shop);
            return shop;
        }
    }

    @BeforeEach
    void setUp() {
        shopChangeHistoryRepository = new RecordingShopChangeHistoryRepository();
        shopRequestIndexRepository = new RecordingShopRequestIndexRepository();
        shopImageApprovalService = new ShopImageApprovalService(
            new FakeShopImageChangeRequestRepository(),
            new FakeShopRepository(),
            new ShopChangeHistoryRecorder(shopChangeHistoryRepository),
            new ShopRequestIndexRecorder(shopRequestIndexRepository)
        );
    }

    @Test
    @DisplayName("상표 변경을 요청하면 TRADEMARK_CHANGE_REQUEST 이력이 정확히 1건 남는다")
    void requestImageChange_recordsExactlyOneTrademarkHistory() {
        shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.TRADEMARK, 4821L, ShopChangeActor.ceo(7L)
        );

        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.TRADEMARK_CHANGE_REQUEST)).hasSize(1);
        ShopChangeHistory history =
            shopChangeHistoryRepository.savedOf(ShopChangeType.TRADEMARK_CHANGE_REQUEST).getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopChangeActionType.CREATE);
        assertThat(history.getActorType()).isEqualTo(ShopChangeActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getPreviousValue()).isNull();
        assertThat(history.getNewValue()).isEqualTo("상표 변경요청(파일 #4821)");
    }

    @Test
    @DisplayName("대표이미지 변경요청은 THUMBNAIL_CHANGE_REQUEST로 분류된다")
    void requestImageChange_recordsThumbnailHistory_whenImageTypeIsThumbnail() {
        shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.THUMBNAIL, 902L, ShopChangeActor.ceo(7L)
        );

        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.TRADEMARK_CHANGE_REQUEST)).isEmpty();
        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.THUMBNAIL_CHANGE_REQUEST))
            .singleElement()
            .satisfies(history ->
                assertThat(history.getNewValue()).isEqualTo("대표이미지 변경요청(파일 #902)"));
    }

    @Test
    @DisplayName("관리자 승인·반려는 변경이력을 남기지 않는다(점주가 한 변경이 아니다)")
    void reviewActions_doNotRecordShopChangeHistory() {
        Long trademarkRequestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.TRADEMARK, 4821L, ShopChangeActor.ceo(7L)
        );
        Long thumbnailRequestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.THUMBNAIL, 902L, ShopChangeActor.ceo(7L)
        );

        shopImageApprovalService.approveImageChange(trademarkRequestId);
        shopImageApprovalService.rejectImageChange(thumbnailRequestId, "해상도가 낮습니다.");

        // 요청 2건에 대한 2행만 남고, 검수 조치로는 추가되지 않는다.
        assertThat(shopChangeHistoryRepository.saved()).hasSize(2);
    }

    @Test
    @DisplayName("이미지 변경을 요청하면 요청 인덱스 1행이 PENDING으로 생긴다")
    void requestImageChange_createsRequestIndexRow() {
        Long requestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.TRADEMARK, 4821L, ShopChangeActor.ceo(7L)
        );

        ShopRequestIndex index = shopRequestIndexRepository.require(ShopRequestType.TRADEMARK_CHANGE, requestId);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.PENDING);
        assertThat(index.getSummary()).isEqualTo("상표 변경요청(파일 #4821)");
        assertThat(index.getRequestedByCeoId()).isEqualTo(7L);
        assertThat(index.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("대표이미지 변경요청은 THUMBNAIL_CHANGE 유형으로 인덱싱된다")
    void requestImageChange_indexesThumbnailType() {
        Long requestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.THUMBNAIL, 902L, ShopChangeActor.ceo(7L)
        );

        assertThat(shopRequestIndexRepository.require(ShopRequestType.THUMBNAIL_CHANGE, requestId).getSummary())
            .isEqualTo("대표이미지 변경요청(파일 #902)");
    }

    @Test
    @DisplayName("승인은 인덱스를 APPROVED로 동기화한다")
    void approveImageChange_syncsRequestIndex() {
        Long requestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.TRADEMARK, 4821L, ShopChangeActor.ceo(7L)
        );

        shopImageApprovalService.approveImageChange(requestId);

        ShopRequestIndex index = shopRequestIndexRepository.require(ShopRequestType.TRADEMARK_CHANGE, requestId);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.APPROVED);
        assertThat(index.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("반려는 인덱스를 REJECTED로 동기화하고 사유를 함께 남긴다")
    void rejectImageChange_syncsRequestIndexWithReason() {
        Long requestId = shopImageApprovalService.requestImageChange(
            SHOP_ID, ShopImageType.THUMBNAIL, 902L, ShopChangeActor.ceo(7L)
        );

        shopImageApprovalService.rejectImageChange(requestId, "해상도가 낮습니다.");

        ShopRequestIndex index = shopRequestIndexRepository.require(ShopRequestType.THUMBNAIL_CHANGE, requestId);
        assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.REJECTED);
        assertThat(index.getRejectReason()).isEqualTo("해상도가 낮습니다.");
        assertThat(index.getProcessedAt()).isNotNull();
    }
}
