package com.tastyhouse.domain.shop.service;

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
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 가게 등록 시 접근권한 이력 기록 봉인 테스트.
 *
 * <p>가게 등록에서 점주를 함께 배정하는 것도 접근권한 부여이므로, {@code ShopCeoAssignmentService}로
 * 나중에 배정한 경우와 구별 없이 {@code GRANT} 이력이 남아야 한다. 반대로 점주 없이 등록하면 부여할
 * 권한이 없으므로 아무 행도 남지 않아야 한다.
 */
class ShopLifecycleServiceTest {

    private static final Long ADMIN_ID = 99L;
    private static final Long CEO_ID = 7L;
    private static final Long STATION_ID = 3L;

    private RecordingShopCeoAssignmentHistoryRepository assignmentHistoryRepository;
    private ShopLifecycleService shopLifecycleService;

    @BeforeEach
    void setUp() {
        assignmentHistoryRepository = new RecordingShopCeoAssignmentHistoryRepository();
        ShopChangeHistoryRecorder changeHistoryRecorder =
            new ShopChangeHistoryRecorder(new RecordingShopChangeHistoryRepository());
        shopLifecycleService = new ShopLifecycleService(
            new FakeShopRepository(),
            null,
            new FakeShopBookmarkRepository(),
            id -> true,
            new ShopImageApprovalService(
                new FakeShopImageChangeRequestRepository(),
                new FakeShopRepository(),
                changeHistoryRecorder,
                new ShopRequestIndexRecorder(new RecordingShopRequestIndexRepository())
            ),
            new ProhibitedWordValidator(new FakeProhibitedWordRepository()),
            changeHistoryRecorder,
            new ShopCeoAssignmentRecorder(assignmentHistoryRepository)
        );
    }

    @Test
    @DisplayName("점주를 지정해 등록하면 GRANT 이력 1행이 남는다")
    void createShop_withCeo_recordsGrant() {
        Shop shop = createShop(CEO_ID);

        assertThat(assignmentHistoryRepository.saved()).hasSize(1);
        ShopCeoAssignmentHistory history = assignmentHistoryRepository.saved().getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopCeoAssignmentActionType.GRANT);
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(CEO_ID));
        assertThat(history.getShopId()).isEqualTo(shop.getShopId());
        assertThat(history.getActorAdminId()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("점주 없이 등록하면 접근권한 이력을 남기지 않는다")
    void createShop_withoutCeo_recordsNothing() {
        createShop(null);

        assertThat(assignmentHistoryRepository.saved()).isEmpty();
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

    /** 가게 write 포트 fake. 저장 시 식별자를 부여해 이력이 실제 가게를 가리키게 한다. */
    private static final class FakeShopRepository implements ShopRepository {

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

    /** 즐겨찾기 write 포트 fake — 이 테스트는 즐겨찾기 경로를 타지 않는다. */
    private static final class FakeShopBookmarkRepository implements ShopBookmarkRepository {

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

    /** 이미지 변경요청 write 포트 fake — PENDING 요청이 없는 상태. */
    private static final class FakeShopImageChangeRequestRepository
        implements ShopImageChangeRequestRepository {

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

    /** 금칙어 read 포트 fake — 이 테스트는 금칙어 검수 경로를 타지 않는다. */
    private static final class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public java.util.List<ProhibitedWord> findAll() {
            return java.util.List.of();
        }
    }
}
