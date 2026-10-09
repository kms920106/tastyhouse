package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.ceo.port.out.write.CeoLoadPort;
import com.tastyhouse.application.ceo.port.out.write.CeoSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;
import com.tastyhouse.testsupport.shop.service.RecordingShopCeoAssignmentHistorySavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ShopCeoAssignmentServiceTest {

    private static final Long SHOP_ID = 12L;
    private static final Long CEO_A = 7L;
    private static final Long CEO_B = 8L;
    private static final Long ADMIN_ID = 99L;

    private FakeShopPersistence shopPersistence;
    private RecordingShopCeoAssignmentHistorySavePort historySavePort;
    private ShopCeoAssignmentService shopCeoAssignmentService;

    @BeforeEach
    void setUp() {
        shopPersistence = new FakeShopPersistence();
        historySavePort = new RecordingShopCeoAssignmentHistorySavePort();
        shopCeoAssignmentService = new ShopCeoAssignmentService(
            shopPersistence,
            shopPersistence,
            new FakeCeoPersistence(),
            new ShopCeoAssignmentRecorder(historySavePort)
        );
    }

    @Test
    @DisplayName("미배정 → 배정: GRANT 1행이 남고 SHOP.ceo_id가 갱신된다")
    void assign_fromUnassigned_recordsSingleGrant() {
        shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_A), ADMIN_ID);

        assertThat(shopPersistence.find().getCeoId()).isEqualTo(CeoId.of(CEO_A));
        assertThat(historySavePort.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(tuple(ShopCeoAssignmentActionType.GRANT, CeoId.of(CEO_A)));
        assertThat(historySavePort.saved().getFirst().getShopId()).isEqualTo(ShopId.of(SHOP_ID));
        assertThat(historySavePort.saved().getFirst().getActorAdminId()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("A 배정 → B 재배정: REVOKE(A) + GRANT(B) 2행이 순서대로 남는다")
    void assign_reassignToAnotherCeo_recordsRevokeThenGrant() {
        shopPersistence.assignCeoA();

        shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_B), ADMIN_ID);

        assertThat(shopPersistence.find().getCeoId()).isEqualTo(CeoId.of(CEO_B));
        assertThat(historySavePort.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(
                tuple(ShopCeoAssignmentActionType.REVOKE, CeoId.of(CEO_A)),
                tuple(ShopCeoAssignmentActionType.GRANT, CeoId.of(CEO_B))
            );
    }

    @Test
    @DisplayName("A 배정 → A 재배정: 409로 거부하고 이력을 남기지 않는다")
    void assign_sameCeoAgain_rejectsWithoutRecording() {
        shopPersistence.assignCeoA();

        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(CEO_A), ADMIN_ID))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(AdminErrorCode.SHOP_CEO_ALREADY_ASSIGNED);

        assertThat(historySavePort.saved()).isEmpty();
        assertThat(shopPersistence.find().getCeoId()).isEqualTo(CeoId.of(CEO_A));
    }

    @Test
    @DisplayName("배정 → 해제: REVOKE 1행이 남고 SHOP.ceo_id가 NULL이 된다")
    void revoke_fromAssigned_recordsSingleRevoke() {
        shopPersistence.assignCeoA();

        shopCeoAssignmentService.revoke(ShopId.of(SHOP_ID), ADMIN_ID);

        assertThat(shopPersistence.find().getCeoId()).isNull();
        assertThat(historySavePort.saved())
            .extracting(ShopCeoAssignmentHistory::getActionType, ShopCeoAssignmentHistory::getCeoId)
            .containsExactly(tuple(ShopCeoAssignmentActionType.REVOKE, CeoId.of(CEO_A)));
    }

    @Test
    @DisplayName("미배정 → 해제: 409로 거부하고 이력을 남기지 않는다")
    void revoke_fromUnassigned_rejectsWithoutRecording() {
        assertThatThrownBy(() -> shopCeoAssignmentService.revoke(ShopId.of(SHOP_ID), ADMIN_ID))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(AdminErrorCode.SHOP_CEO_NOT_ASSIGNED);

        assertThat(historySavePort.saved()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 점주 배정: 404로 거부하고 이력·배정을 남기지 않는다")
    void assign_unknownCeo_rejectsWithoutRecording() {
        Long unknownCeoId = 404L;

        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(SHOP_ID), CeoId.of(unknownCeoId), ADMIN_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(e -> ((ResourceNotFoundException) e).getErrorCode())
            .isEqualTo(AdminErrorCode.CEO_NOT_FOUND);

        assertThat(historySavePort.saved()).isEmpty();
        assertThat(shopPersistence.find().getCeoId()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 가게: 404로 거부한다")
    void assign_unknownShop_rejects() {
        assertThatThrownBy(() ->
            shopCeoAssignmentService.assign(ShopId.of(999L), CeoId.of(CEO_A), ADMIN_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(e -> ((ResourceNotFoundException) e).getErrorCode())
            .isEqualTo(ApplicationErrorCode.SHOP_NOT_FOUND);

        assertThat(historySavePort.saved()).isEmpty();
    }

    private static final class FakeShopPersistence implements ShopLoadPort, ShopSavePort {

        private final Map<Long, Shop> shops = new HashMap<>();

        FakeShopPersistence() {
            shops.put(SHOP_ID, Shop.reconstitute(
                SHOP_ID, null, null, "맛있는 분식",
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, false, false, false, 10000, false, false, false, null, null
            ));
        }

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

    private static final class FakeCeoPersistence implements CeoLoadPort, CeoSavePort {

        private final Map<Long, Ceo> ceos = new HashMap<>();

        FakeCeoPersistence() {
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
