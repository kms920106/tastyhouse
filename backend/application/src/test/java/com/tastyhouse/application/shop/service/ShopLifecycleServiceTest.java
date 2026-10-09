package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;
import com.tastyhouse.testsupport.shop.service.RecordingShopCeoAssignmentHistorySavePort;
import com.tastyhouse.testsupport.shop.service.RecordingShopChangeHistorySavePort;
import com.tastyhouse.testsupport.shop.service.RecordingShopRequestIndexPersistence;

import static org.assertj.core.api.Assertions.assertThat;

class ShopLifecycleServiceTest {

    private static final Long ADMIN_ID = 99L;
    private static final Long CEO_ID = 7L;
    private static final Long STATION_ID = 3L;

    private RecordingShopCeoAssignmentHistorySavePort assignmentHistorySavePort;
    private ShopLifecycleService shopLifecycleService;

    @BeforeEach
    void setUp() {
        assignmentHistorySavePort = new RecordingShopCeoAssignmentHistorySavePort();
        ShopChangeHistoryRecorder changeHistoryRecorder =
            new ShopChangeHistoryRecorder(new RecordingShopChangeHistorySavePort());
        FakeShopPersistence fakeShopPersistence = new FakeShopPersistence();
        FakeShopBookmarkPersistence fakeShopBookmarkPersistence = new FakeShopBookmarkPersistence();
        FakeShopImageChangeRequestPersistence fakeShopImageChangeRequestPersistence = new FakeShopImageChangeRequestPersistence();
        FakeShopPersistence imageApprovalShopPersistence = new FakeShopPersistence();
        RecordingShopRequestIndexPersistence recordingShopRequestIndexPersistence = new RecordingShopRequestIndexPersistence();
        shopLifecycleService = new ShopLifecycleService(
            fakeShopPersistence,
            fakeShopPersistence,
            null,
            null,
            fakeShopBookmarkPersistence,
            fakeShopBookmarkPersistence,
            id -> true,
            new ShopImageApprovalService(
                fakeShopImageChangeRequestPersistence,
                fakeShopImageChangeRequestPersistence,
                imageApprovalShopPersistence,
                imageApprovalShopPersistence,
                changeHistoryRecorder,
                new ShopRequestIndexRecorder(recordingShopRequestIndexPersistence, recordingShopRequestIndexPersistence)
            ),
            new ProhibitedWordValidator(new FakeProhibitedWordLoadPort()),
            changeHistoryRecorder,
            new ShopCeoAssignmentRecorder(assignmentHistorySavePort)
        );
    }

    @Test
    @DisplayName("점주를 지정해 등록하면 GRANT 이력 1행이 남는다")
    void createShop_withCeo_recordsGrant() {
        Shop shop = createShop(CEO_ID);

        assertThat(assignmentHistorySavePort.saved()).hasSize(1);
        ShopCeoAssignmentHistory history = assignmentHistorySavePort.saved().getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopCeoAssignmentActionType.GRANT);
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(CEO_ID));
        assertThat(history.getShopId()).isEqualTo(shop.getShopId());
        assertThat(history.getActorAdminId()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("점주 없이 등록하면 접근권한 이력을 남기지 않는다")
    void createShop_withoutCeo_recordsNothing() {
        createShop(null);

        assertThat(assignmentHistorySavePort.saved()).isEmpty();
    }

    private Shop createShop(Long ceoId) {
        return shopLifecycleService.createShop(
            ADMIN_ID,
            ceoId,
            STATION_ID,
            "맛있는 분식",
            BigDecimal.valueOf(37.497942),
            BigDecimal.valueOf(127.027621),
            "서울시 송파구 위례성대로 10",
            "서울시 송파구 방이동 44-1",
            "02-1234-5678",
            null
        );
    }

    private static final class FakeShopPersistence implements ShopLoadPort, ShopSavePort {

        private final Map<Long, Shop> shops = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong();

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
            Long id = shop.getId() == null ? sequence.incrementAndGet() : shop.getId();
            Shop persisted = Shop.reconstitute(
                id,
                shop.getCeoId(),
                shop.getStationId(),
                shop.getName(),
                shop.getLatitude(),
                shop.getLongitude(),
                shop.getRating(),
                shop.getRoadAddress(),
                shop.getLotAddress(),
                shop.getPhoneNumber(),
                shop.getThumbnailImageFileId(),
                shop.getTrademarkImageFileId(),
                shop.isPermanentlyClosed(),
                shop.isHidden(),
                shop.isClosedOnPublicHolidays(),
                shop.getMinOrderAmount(),
                shop.isScheduledOrderEnabled(),
                false,
                false,
                null,
                null
            );
            shops.put(id, persisted);
            return persisted;
        }
    }

    private static final class FakeShopBookmarkPersistence implements ShopBookmarkLoadPort, ShopBookmarkSavePort {

        @Override
        public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
            return false;
        }

        @Override
        public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
        }

        @Override
        public ShopBookmark save(ShopBookmark shopBookmark) {
            return shopBookmark;
        }
    }

    private static final class FakeShopImageChangeRequestPersistence
        implements ShopImageChangeRequestLoadPort, ShopImageChangeRequestSavePort {

        @Override
        public Optional<ShopImageChangeRequest> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByShopIdAndImageTypeAndStatus(
            Long shopId,
            com.tastyhouse.domain.shop.model.ShopImageType imageType,
            ApprovalStatus status
        ) {
            return false;
        }

        @Override
        public boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status) {
            return false;
        }

        @Override
        public ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest) {
            return shopImageChangeRequest;
        }
    }

    private static final class FakeProhibitedWordLoadPort implements ProhibitedWordLoadPort {

        @Override
        public java.util.List<ProhibitedWord> findAll() {
            return java.util.List.of();
        }
    }
}
