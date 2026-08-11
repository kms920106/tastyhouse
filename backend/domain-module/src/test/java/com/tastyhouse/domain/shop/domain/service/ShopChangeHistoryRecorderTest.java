package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 가게 변경이력 기록기 단위 테스트. Spring 컨텍스트 없이 fake 포트만으로 검증한다.
 */
class ShopChangeHistoryRecorderTest {

    @Test
    @DisplayName("record는 전달받은 값으로 이력 1행을 저장한다")
    void record_savesSingleHistoryRow() {
        FakeShopChangeHistoryRepository repository = new FakeShopChangeHistoryRepository();
        ShopChangeHistoryRecorder recorder = new ShopChangeHistoryRecorder(repository);

        recorder.record(
            ShopId.of(7L),
            ShopChangeType.DELIVERY_TIP_SCHEDULE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(42L),
            "18:00~20:00: +1,000원",
            "18:00~20:00: +1,500원"
        );

        assertThat(repository.saved).hasSize(1);
        ShopChangeHistory history = repository.saved.getFirst();
        assertThat(history.getShopId()).isEqualTo(ShopId.of(7L));
        assertThat(history.getChangeType()).isEqualTo(ShopChangeType.DELIVERY_TIP_SCHEDULE);
        assertThat(history.getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(history.getActorType()).isEqualTo(ShopChangeActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(42L);
        assertThat(history.getPreviousValue()).isEqualTo("18:00~20:00: +1,000원");
        assertThat(history.getNewValue()).isEqualTo("18:00~20:00: +1,500원");
    }

    @Test
    @DisplayName("대분류는 중분류에서 파생되므로 따로 전달하지 않아도 채워진다")
    void record_derivesCategoryFromChangeType() {
        FakeShopChangeHistoryRepository repository = new FakeShopChangeHistoryRepository();
        ShopChangeHistoryRecorder recorder = new ShopChangeHistoryRecorder(repository);

        recorder.record(
            ShopId.of(1L),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(1L),
            null,
            "매일 09:00~22:00"
        );

        assertThat(repository.saved.getFirst().getCategory()).isEqualTo(ShopChangeCategory.OPERATION);
    }

    @Test
    @DisplayName("등록은 previousValue가, 삭제는 newValue가 null로 남는다")
    void record_keepsNullSideForCreateAndDelete() {
        FakeShopChangeHistoryRepository repository = new FakeShopChangeHistoryRepository();
        ShopChangeHistoryRecorder recorder = new ShopChangeHistoryRecorder(repository);

        recorder.record(ShopId.of(1L), ShopChangeType.CLOSED_DAY, ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(1L), null, "매주 월요일");
        recorder.record(ShopId.of(1L), ShopChangeType.CLOSED_DAY, ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(1L), "매주 월요일", null);

        assertThat(repository.saved.get(0).getPreviousValue()).isNull();
        assertThat(repository.saved.get(0).getNewValue()).isEqualTo("매주 월요일");
        assertThat(repository.saved.get(1).getPreviousValue()).isEqualTo("매주 월요일");
        assertThat(repository.saved.get(1).getNewValue()).isNull();
    }

    private static class FakeShopChangeHistoryRepository implements ShopChangeHistoryRepository {

        private final List<ShopChangeHistory> saved = new ArrayList<>();

        @Override
        public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
            saved.add(shopChangeHistory);
            return shopChangeHistory;
        }
    }
}
