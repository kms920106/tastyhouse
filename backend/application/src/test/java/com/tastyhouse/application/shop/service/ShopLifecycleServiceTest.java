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
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

import static org.assertj.core.api.Assertions.assertThat;

class ShopLifecycleServiceTest {
    private static final Long ADMIN_ID = 99L;
    private static final Long CEO_ID = 7L;
    private static final Long STATION_ID = 3L;

    private RecordingShopCeoAssignmentHistoryPersistencePort assignmentHistoryPersistencePort;
    private ShopLifecycleService shopLifecycleService;

    @BeforeEach
    void setUp() {
        assignmentHistoryPersistencePort = new RecordingShopCeoAssignmentHistoryPersistencePort();
        ShopChangeHistoryRecorder changeHistoryRecorder =
            new ShopChangeHistoryRecorder(new RecordingShopChangeHistoryPersistencePort());
        shopLifecycleService = new ShopLifecycleService(
            new FakeShopPersistencePort(),
            null,
            new FakeShopBookmarkPersistencePort(),
            id -> true,
            new ShopImageApprovalService(
                new FakeShopImageChangeRequestPersistencePort(),
                new FakeShopPersistencePort(),
                changeHistoryRecorder,
                new ShopRequestIndexRecorder(new RecordingShopRequestIndexPersistencePort())
            ),
            new ProhibitedWordValidator(new FakeProhibitedWordPersistencePort()),
            changeHistoryRecorder,
            new ShopCeoAssignmentRecorder(assignmentHistoryPersistencePort)
        );
    }

    @Test
    @DisplayName("점주를 지정해 등록하면 GRANT 이력 1행이 남는다")
    void createShop_withCeo_recordsGrant() {
        Shop shop = createShop(CEO_ID);

        assertThat(assignmentHistoryPersistencePort.saved()).hasSize(1);
        ShopCeoAssignmentHistory history = assignmentHistoryPersistencePort.saved().getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopCeoAssignmentActionType.GRANT);
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(CEO_ID));
        assertThat(history.getShopId()).isEqualTo(shop.getShopId());
        assertThat(history.getActorAdminId()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("점주 없이 등록하면 접근권한 이력을 남기지 않는다")
    void createShop_withoutCeo_recordsNothing() {
        createShop(null);

        assertThat(assignmentHistoryPersistencePort.saved()).isEmpty();
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

    private static final class FakeShopPersistencePort implements ShopPersistencePort {
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

    private static final class FakeShopBookmarkPersistencePort implements ShopBookmarkPersistencePort {
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

    private static final class FakeShopImageChangeRequestPersistencePort
        implements ShopImageChangeRequestPersistencePort {
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

    private static final class FakeProhibitedWordPersistencePort implements ProhibitedWordPersistencePort {
        @Override
        public java.util.List<ProhibitedWord> findAll() {
            return java.util.List.of();
        }
    }
}
