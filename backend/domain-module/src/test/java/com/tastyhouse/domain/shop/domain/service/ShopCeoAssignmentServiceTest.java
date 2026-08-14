package com.tastyhouse.domain.shop.domain.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentRecorder;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * 가게 담당 점주 배정·해제 상태 규칙 봉인 테스트.
 *
 * <p>{@code ShopCeoAssignmentService} Javadoc의 상태 규칙 표 전체를 항목별로 검증한다. 특히
 * <b>재배정이 {@code REVOKE}+{@code GRANT} 2행</b>인 것을 봉인한다 — 한 행에 before/after를 담는
 * 형태로 되돌아가면 "언제부터 언제까지 권한이 있었는가"를 읽을 수 없게 된다.
 */
class ShopCeoAssignmentServiceTest {

    private static final Long SHOP_ID = 12L;
    private static final Long CEO_A = 7L;
    private static final Long CEO_B = 8L;
    private static final Long ADMIN_ID = 99L;

    private FakeShopRepository shopRepository;
    private RecordingShopCeoAssignmentHistoryRepository historyRepository;
    private ShopCeoAssignmentService shopCeoAssignmentService;

    @BeforeEach
    void setUp() {
        shopRepository = new FakeShopRepository();
        historyRepository = new RecordingShopCeoAssignmentHistoryRepository();
        shopCeoAssignmentService = new ShopCeoAssignmentService(
            shopRepository,
            new FakeCeoRepository(),
            new ShopCeoAssignmentRecorder(historyRepository)
        );
    }

    @Test
    @DisplayName("미배정 → 배정: GRANT 1행이 남고 SHOP.ceo_id가 갱신된다")
    void assign_fromUnassigned_recordsSingleGrant() {
        shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_A), ADMIN_ID);

        assertThat(shopRepository.find().getCeoId()).isEqualTo(CeoId.of(CEO_A));
        assertThat(historyRepository.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(tuple(ShopCeoAssignmentActionType.GRANT, CeoId.of(CEO_A)));
        assertThat(historyRepository.saved().getFirst().getShopId()).isEqualTo(ShopId.of(SHOP_ID));
        assertThat(historyRepository.saved().getFirst().getActorAdminId()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("A 배정 → B 재배정: REVOKE(A) + GRANT(B) 2행이 순서대로 남는다")
    void assign_reassignToAnotherCeo_recordsRevokeThenGrant() {
        shopRepository.assignCeoA();

        shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_B), ADMIN_ID);

        assertThat(shopRepository.find().getCeoId()).isEqualTo(CeoId.of(CEO_B));
        assertThat(historyRepository.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(
                tuple(ShopCeoAssignmentActionType.REVOKE, CeoId.of(CEO_A)),
                tuple(ShopCeoAssignmentActionType.GRANT, CeoId.of(CEO_B))
            );
    }

    @Test
    @DisplayName("A 배정 → A 재배정: 409로 거부하고 이력을 남기지 않는다")
    void assign_sameCeoAgain_rejectsWithoutRecording() {
        shopRepository.assignCeoA();

        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_A), ADMIN_ID))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CEO_ALREADY_ASSIGNED);

        assertThat(historyRepository.saved()).isEmpty();
        assertThat(shopRepository.find().getCeoId()).isEqualTo(CeoId.of(CEO_A));
    }

    @Test
    @DisplayName("배정 → 해제: REVOKE 1행이 남고 SHOP.ceo_id가 NULL이 된다")
    void revoke_fromAssigned_recordsSingleRevoke() {
        shopRepository.assignCeoA();

        shopCeoAssignmentService.revoke(ShopId.of(SHOP_ID), ADMIN_ID);

        assertThat(shopRepository.find().getCeoId()).isNull();
        assertThat(historyRepository.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(tuple(ShopCeoAssignmentActionType.REVOKE, CeoId.of(CEO_A)));
    }

    @Test
    @DisplayName("미배정 → 해제: 409로 거부하고 이력을 남기지 않는다")
    void revoke_fromUnassigned_rejectsWithoutRecording() {
        assertThatThrownBy(() -> shopCeoAssignmentService.revoke(ShopId.of(SHOP_ID), ADMIN_ID))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CEO_NOT_ASSIGNED);

        assertThat(historyRepository.saved()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 점주 배정: 404로 거부하고 이력·배정을 남기지 않는다")
    void assign_unknownCeo_rejectsWithoutRecording() {
        Long unknownCeoId = 404L;

        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(unknownCeoId), ADMIN_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(e -> ((ResourceNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.CEO_NOT_FOUND);

        assertThat(historyRepository.saved()).isEmpty();
        assertThat(shopRepository.find().getCeoId()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 가게: 404로 거부한다")
    void assign_unknownShop_rejects() {
        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(999L), CeoId.of(CEO_A), ADMIN_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(e -> ((ResourceNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_NOT_FOUND);

        assertThat(historyRepository.saved()).isEmpty();
    }

    /** 가게 write 포트 fake. 점주 미배정 상태로 시작한다. */
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

        /** 테스트 착수 상태를 만든다 — 이미 {@link ShopCeoAssignmentServiceTest#CEO_A}가 배정된 가게. */
        void assignCeoA() {
            shops.get(SHOP_ID).assignCeo(CeoId.of(CEO_A));
        }

        Shop find() {
            return shops.get(SHOP_ID);
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

    /** 점주 write 포트 fake. {@link #CEO_A}·{@link #CEO_B}만 실재한다. */
    private static final class FakeCeoRepository implements CeoRepository {

        private final Map<Long, Ceo> ceos = new HashMap<>();

        FakeCeoRepository() {
            ceos.put(CEO_A, Ceo.reconstitute(CEO_A, "ceoA", "encoded", "점주A", null, null, null, null));
            ceos.put(CEO_B, Ceo.reconstitute(CEO_B, "ceoB", "encoded", "점주B", null, null, null, null));
        }

        @Override
        public Optional<Ceo> findById(CeoId id) {
            return Optional.ofNullable(ceos.get(id.value()));
        }

        @Override
        public Optional<Ceo> findByUsername(String username) {
            return ceos.values().stream()
                .filter(ceo -> ceo.getUsername().equals(username))
                .findFirst();
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username).isPresent();
        }

        @Override
        public Ceo save(Ceo ceo) {
            ceos.put(ceo.getId(), ceo);
            return ceo;
        }
    }
}
