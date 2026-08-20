package com.tastyhouse.domain.shop.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 배달팁 컬렉션 불변식 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 세 개의 포트를 손으로 만든 fake로 대체해 검증한다
 * (domain-module에는 Mockito 의존이 없다).
 */
class ShopDeliveryTipServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final Long DONG_A = 100L;
    private static final Long DONG_B = 200L;

    private final ShopDeliveryTipRepositoryFake tipRepository = new ShopDeliveryTipRepositoryFake();
    private final ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
    private final AdminDongRepositoryFake adminDongRepository = new AdminDongRepositoryFake();
    private final RecordingShopChangeHistoryRepository historyRepository =
        new RecordingShopChangeHistoryRepository();
    private final ShopDeliveryTipService service = new ShopDeliveryTipService(
        tipRepository, areaRepository, adminDongRepository, new ShopChangeHistoryRecorder(historyRepository)
    );

    private static final ShopChangeActor ACTOR = ShopChangeActor.ceo(9L);

    @Nested
    @DisplayName("replaceTiers")
    class ReplaceTiers {

        @Test
        @DisplayName("구간이 0개면 SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED로 거부한다")
        void replaceTiers_rejectsEmpty() {
            assertThatThrownBy(() -> service.replaceTiers(SHOP_ID, List.of(), ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("구간이 최대 3개를 넘으면 SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED로 거부한다")
        void replaceTiers_rejectsMoreThanMax() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(5000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 1500),
                ShopDeliveryTipTierSpec.of(15000, 1000),
                ShopDeliveryTipTierSpec.of(20000, 500)
            );

            assertThatThrownBy(() -> service.replaceTiers(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("하한 주문금액이 중복되면 SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING으로 거부한다")
        void replaceTiers_rejectsDuplicatedMinOrderAmount() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(10000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 1500)
            );

            assertThatThrownBy(() -> service.replaceTiers(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING);
        }

        @Test
        @DisplayName("주문금액이 올라가는데 배달팁이 내려가지 않으면 SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING으로 거부한다")
        void replaceTiers_rejectsNonDescendingTip() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(5000, 1500),
                ShopDeliveryTipTierSpec.of(10000, 2000)
            );

            assertThatThrownBy(() -> service.replaceTiers(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING);
        }

        @Test
        @DisplayName("팁이 같은 값으로 유지돼도(strict 내림차순 위반) 거부한다")
        void replaceTiers_rejectsEqualTip() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(5000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 2000)
            );

            assertThatThrownBy(() -> service.replaceTiers(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING);
        }

        @Test
        @DisplayName("입력 순서가 뒤섞여 와도 주문금액 오름차순으로 정렬해 저장한다")
        void replaceTiers_sortsUnorderedInput() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(15000, 1000),
                ShopDeliveryTipTierSpec.of(5000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 1500)
            );

            List<ShopDeliveryTipTier> saved = service.replaceTiers(SHOP_ID, specs, ACTOR);

            assertThat(saved).extracting(ShopDeliveryTipTier::getMinOrderAmount)
                .containsExactly(5000, 10000, 15000);
            assertThat(saved).extracting(ShopDeliveryTipTier::getTipAmount)
                .containsExactly(2000, 1500, 1000);
        }

        @Test
        @DisplayName("tier_order는 호출부가 보낸 순서가 아니라 정렬 후 0..n-1로 재부여한다")
        void replaceTiers_reassignsTierOrder() {
            List<ShopDeliveryTipTierSpec> specs = List.of(
                ShopDeliveryTipTierSpec.of(15000, 1000),
                ShopDeliveryTipTierSpec.of(5000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 1500)
            );

            List<ShopDeliveryTipTier> saved = service.replaceTiers(SHOP_ID, specs, ACTOR);

            assertThat(saved).extracting(ShopDeliveryTipTier::getTierOrder).containsExactly(0, 1, 2);
        }

        @Test
        @DisplayName("교체는 replace-all이라 기존 구간을 먼저 삭제한다")
        void replaceTiers_deletesExistingFirst() {
            service.replaceTiers(SHOP_ID, List.of(
                ShopDeliveryTipTierSpec.of(5000, 2000),
                ShopDeliveryTipTierSpec.of(10000, 1500)
            ), ACTOR);

            service.replaceTiers(SHOP_ID, List.of(ShopDeliveryTipTierSpec.of(3000, 2500)), ACTOR);

            assertThat(tipRepository.findTiersByShopId(SHOP_ID))
                .extracting(ShopDeliveryTipTier::getMinOrderAmount)
                .containsExactly(3000);
        }
    }

    @Nested
    @DisplayName("거리별 ↔ 지역별 배타")
    class ExtraTypeExclusivity {

        @Test
        @DisplayName("지역별 팁이 있는 상태에서 거리별 설정은 SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT로 거부한다")
        void changeDistanceTip_rejectsWhenRegionTipExists() {
            registerDeliveryArea(DONG_A);
            service.replaceRegionTips(SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR);

            assertThatThrownBy(() -> service.changeDistanceTip(SHOP_ID, 1500, DeliveryTipDistanceUnit.PER_500M, 500, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT);
        }

        @Test
        @DisplayName("거리별 설정 상태에서 비어 있지 않은 지역별 교체는 SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT로 거부한다")
        void replaceRegionTips_rejectsWhenDistanceConfigured() {
            registerDeliveryArea(DONG_A);
            service.changeDistanceTip(SHOP_ID, 1500, DeliveryTipDistanceUnit.PER_500M, 500, ACTOR);

            assertThatThrownBy(() -> service.replaceRegionTips(
                SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT);
        }

        @Test
        @DisplayName("지역별을 전부 삭제하면 설정이 NONE으로 돌아가 거리별 설정에 성공한다")
        void replaceRegionTips_emptyThenDistanceSucceeds() {
            registerDeliveryArea(DONG_A);
            service.replaceRegionTips(SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR);
            assertThat(tipRepository.findSettingByShopId(SHOP_ID).orElseThrow().getExtraTipType())
                .isEqualTo(DeliveryTipExtraType.REGION);

            service.replaceRegionTips(SHOP_ID, List.of(), ACTOR);

            assertThat(tipRepository.findRegionTipsByShopId(SHOP_ID)).isEmpty();
            assertThat(tipRepository.findSettingByShopId(SHOP_ID).orElseThrow().getExtraTipType())
                .isEqualTo(DeliveryTipExtraType.NONE);

            ShopDeliveryTipSetting setting = service.changeDistanceTip(
                SHOP_ID, 1500, DeliveryTipDistanceUnit.PER_500M, 500, ACTOR
            );

            assertThat(setting.getExtraTipType()).isEqualTo(DeliveryTipExtraType.DISTANCE);
            assertThat(setting.getBaseDistanceMeters()).isEqualTo(1500);
            assertThat(setting.getSurchargeUnit()).isEqualTo(DeliveryTipDistanceUnit.PER_500M);
            assertThat(setting.getSurchargeAmount()).isEqualTo(500);
        }

        @Test
        @DisplayName("clearDistanceTip 후에는 지역별 설정에 성공한다")
        void clearDistanceTip_thenRegionSucceeds() {
            registerDeliveryArea(DONG_A);
            service.changeDistanceTip(SHOP_ID, 1500, DeliveryTipDistanceUnit.PER_500M, 500, ACTOR);

            service.clearDistanceTip(SHOP_ID, ACTOR);

            assertThatCode(() -> service.replaceRegionTips(
                SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR
            )).doesNotThrowAnyException();
            assertThat(tipRepository.findSettingByShopId(SHOP_ID).orElseThrow().getExtraTipType())
                .isEqualTo(DeliveryTipExtraType.REGION);
        }
    }

    @Nested
    @DisplayName("replaceRegionTips")
    class ReplaceRegionTips {

        @Test
        @DisplayName("요청 내 같은 행정동이 두 번 오면 SHOP_DELIVERY_TIP_REGION_DUPLICATED로 거부한다")
        void replaceRegionTips_rejectsDuplicatedAdminDong() {
            registerDeliveryArea(DONG_A);

            assertThatThrownBy(() -> service.replaceRegionTips(SHOP_ID, List.of(
                ShopDeliveryTipRegionSpec.of(DONG_A, 800),
                ShopDeliveryTipRegionSpec.of(DONG_A, 1200)
            ), ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_REGION_DUPLICATED);
        }

        @Test
        @DisplayName("행정동 마스터에 없으면 ADMIN_DONG_NOT_FOUND로 거부한다")
        void replaceRegionTips_rejectsUnknownAdminDong() {
            assertThatThrownBy(() -> service.replaceRegionTips(
                SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        @Test
        @DisplayName("가게 배달가능지역이 아니면 SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA로 거부한다")
        void replaceRegionTips_rejectsRegionOutsideDeliveryArea() {
            adminDongRepository.add(DONG_B);

            assertThatThrownBy(() -> service.replaceRegionTips(
                SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_B, 800)), ACTOR
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA);
        }

        @Test
        @DisplayName("검증을 통과한 지역별 팁은 저장되고 설정이 REGION으로 바뀐다")
        void replaceRegionTips_savesAndSwitchesToRegion() {
            registerDeliveryArea(DONG_A);
            registerDeliveryArea(DONG_B);

            List<ShopDeliveryTipRegion> saved = service.replaceRegionTips(SHOP_ID, List.of(
                ShopDeliveryTipRegionSpec.of(DONG_A, 800),
                ShopDeliveryTipRegionSpec.of(DONG_B, 1200)
            ), ACTOR);

            assertThat(saved).extracting(regionTip -> regionTip.getAdminDongId().value())
                .containsExactly(DONG_A, DONG_B);
            assertThat(saved).extracting(ShopDeliveryTipRegion::getTipAmount).containsExactly(800, 1200);
            assertThat(tipRepository.findSettingByShopId(SHOP_ID).orElseThrow().getExtraTipType())
                .isEqualTo(DeliveryTipExtraType.REGION);
        }

        @Test
        @DisplayName("clearRegionTips는 전부 삭제하고 설정을 NONE으로 되돌린다")
        void clearRegionTips_removesAllAndResetsSetting() {
            registerDeliveryArea(DONG_A);
            service.replaceRegionTips(SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR);

            service.clearRegionTips(SHOP_ID, ACTOR);

            assertThat(tipRepository.findRegionTipsByShopId(SHOP_ID)).isEmpty();
            assertThat(tipRepository.findSettingByShopId(SHOP_ID).orElseThrow().getExtraTipType())
                .isEqualTo(DeliveryTipExtraType.NONE);
        }
    }

    @Nested
    @DisplayName("replaceScheduleTips")
    class ReplaceScheduleTips {

        @Test
        @DisplayName("같은 요일 구분에서 시간대가 겹치면 SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP으로 거부한다")
        void replaceScheduleTips_rejectsOverlapInSameDayType() {
            List<ShopDeliveryTipScheduleSpec> specs = List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000),
                ShopDeliveryTipScheduleSpec.of(DayType.MONDAY, LocalTime.of(20, 0), LocalTime.of(22, 0), 1500)
            );

            assertThatThrownBy(() -> service.replaceScheduleTips(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP);
        }

        @Test
        @DisplayName("자정 넘김 구간(22:00~02:00)과 01:00~03:00의 겹침도 잡아낸다")
        void replaceScheduleTips_detectsOvernightOverlap() {
            List<ShopDeliveryTipScheduleSpec> specs = List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.DAILY, LocalTime.of(22, 0), LocalTime.of(2, 0), 1000),
                ShopDeliveryTipScheduleSpec.of(DayType.DAILY, LocalTime.of(1, 0), LocalTime.of(3, 0), 1500)
            );

            assertThatThrownBy(() -> service.replaceScheduleTips(SHOP_ID, specs, ACTOR))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP);
        }

        @Test
        @DisplayName("같은 요일 구분이라도 맞닿기만 하면(반열림) 겹침이 아니다")
        void replaceScheduleTips_allowsAdjacentRanges() {
            List<ShopDeliveryTipScheduleSpec> specs = List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 0), 1000),
                ShopDeliveryTipScheduleSpec.of(DayType.MONDAY, LocalTime.of(20, 0), LocalTime.of(22, 0), 1500)
            );

            List<ShopDeliveryTipSchedule> saved = service.replaceScheduleTips(SHOP_ID, specs, ACTOR);

            assertThat(saved).hasSize(2);
        }

        @Test
        @DisplayName("요일 구분이 다르면 시간대가 겹쳐도 통과한다 — 적용 시점에 구체성 우선으로 하나만 선택된다")
        void replaceScheduleTips_allowsOverlapAcrossDayTypes() {
            List<ShopDeliveryTipScheduleSpec> specs = List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000),
                ShopDeliveryTipScheduleSpec.of(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 2500)
            );

            List<ShopDeliveryTipSchedule> saved = service.replaceScheduleTips(SHOP_ID, specs, ACTOR);

            assertThat(saved).extracting(ShopDeliveryTipSchedule::getDayType)
                .containsExactly(DayType.DAILY, DayType.MONDAY);
        }

        @Test
        @DisplayName("빈 목록을 보내면 전부 삭제된다")
        void replaceScheduleTips_emptyRemovesAll() {
            service.replaceScheduleTips(SHOP_ID, List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)
            ), ACTOR);

            service.replaceScheduleTips(SHOP_ID, List.of(), ACTOR);

            assertThat(tipRepository.findScheduleTipsByShopId(SHOP_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("changeHolidayTip")
    class ChangeHolidayTip {

        @Test
        @DisplayName("0원은 삭제로 해석해 null을 반환하고 저장된 공휴일 팁을 지운다")
        void changeHolidayTip_zeroDeletes() {
            service.changeHolidayTip(SHOP_ID, 1500, ACTOR);
            assertThat(tipRepository.findHolidayTipByShopId(SHOP_ID)).isPresent();

            ShopDeliveryTipHoliday result = service.changeHolidayTip(SHOP_ID, 0, ACTOR);

            assertThat(result).isNull();
            assertThat(tipRepository.findHolidayTipByShopId(SHOP_ID)).isEmpty();
            assertThat(tipRepository.holidayTipDeleteCount).isEqualTo(1);
        }

        @Test
        @DisplayName("0원이 아니면 upsert한다 — 기존 행이 있으면 금액만 갱신한다")
        void changeHolidayTip_upserts() {
            ShopDeliveryTipHoliday created = service.changeHolidayTip(SHOP_ID, 1500, ACTOR);

            assertThat(created).isNotNull();
            assertThat(created.getTipAmount()).isEqualTo(1500);

            ShopDeliveryTipHoliday updated = service.changeHolidayTip(SHOP_ID, 2500, ACTOR);

            assertThat(updated.getTipAmount()).isEqualTo(2500);
            assertThat(tipRepository.findHolidayTipByShopId(SHOP_ID).orElseThrow().getTipAmount()).isEqualTo(2500);
        }
    }

    @Nested
    @DisplayName("변경이력")
    class ChangeHistory {

        @Test
        @DisplayName("구간 replace-all은 행 수와 무관하게 이력 1행만 남기고 변경 전·후 전체를 담는다")
        void replaceTiers_recordsSingleSnapshotRow() {
            service.replaceTiers(SHOP_ID, List.of(ShopDeliveryTipTierSpec.of(5000, 2000)), ACTOR);

            service.replaceTiers(SHOP_ID, List.of(
                ShopDeliveryTipTierSpec.of(5000, 2500),
                ShopDeliveryTipTierSpec.of(10000, 2000),
                ShopDeliveryTipTierSpec.of(15000, 1500)
            ), ACTOR);

            List<ShopChangeHistory> tierHistories =
                historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_TIER);
            assertThat(tierHistories).hasSize(2);

            ShopChangeHistory second = tierHistories.get(1);
            assertThat(second.getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
            assertThat(second.getPreviousValue()).isEqualTo("5,000원 이상: 2,000원");
            assertThat(second.getNewValue()).isEqualTo(
                "5,000원 이상: 2,500원\n10,000원 이상: 2,000원\n15,000원 이상: 1,500원"
            );
        }

        @Test
        @DisplayName("지역별 replace-all은 이력 1행만 남기고 행정동 이름으로 요약한다")
        void replaceRegionTips_recordsSingleSnapshotRow() {
            registerDeliveryArea(DONG_A);

            service.replaceRegionTips(SHOP_ID, List.of(ShopDeliveryTipRegionSpec.of(DONG_A, 800)), ACTOR);

            List<ShopChangeHistory> histories =
                historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_REGION);
            assertThat(histories).hasSize(1);
            assertThat(histories.getFirst().getPreviousValue()).isEqualTo("없음");
            assertThat(histories.getFirst().getNewValue()).isEqualTo("역삼1동: +800원");
        }

        @Test
        @DisplayName("시간별 replace-all은 이력 1행만 남기고 요일·시간대·금액으로 요약한다")
        void replaceScheduleTips_recordsSingleSnapshotRow() {
            service.replaceScheduleTips(SHOP_ID, List.of(
                ShopDeliveryTipScheduleSpec.of(DayType.WEEKDAY, LocalTime.of(18, 0), LocalTime.of(20, 0), 1500)
            ), ACTOR);

            List<ShopChangeHistory> histories =
                historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_SCHEDULE);
            assertThat(histories).hasSize(1);
            assertThat(histories.getFirst().getNewValue()).isEqualTo("평일 18:00~20:00: +1,500원");
        }

        @Test
        @DisplayName("거리별 설정은 기본거리·단위·금액을 담고, 해제는 DELETE로 남긴다")
        void distanceTip_recordsUpdateThenDelete() {
            service.changeDistanceTip(SHOP_ID, 1500, DeliveryTipDistanceUnit.PER_500M, 500, ACTOR);
            service.clearDistanceTip(SHOP_ID, ACTOR);

            List<ShopChangeHistory> histories =
                historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_DISTANCE);
            assertThat(histories).hasSize(2);
            assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
            assertThat(histories.getFirst().getPreviousValue()).isEqualTo("미설정");
            assertThat(histories.getFirst().getNewValue()).isEqualTo("1.5km까지: 500m당 500원");
            assertThat(histories.get(1).getActionType()).isEqualTo(ShopChangeActionType.DELETE);
            assertThat(histories.get(1).getPreviousValue()).isEqualTo("1.5km까지: 500m당 500원");
            assertThat(histories.get(1).getNewValue()).isNull();
        }

        @Test
        @DisplayName("설정 헤더가 없는 가게의 거리별 해제는 이력을 남기지 않는다")
        void clearDistanceTip_withoutSetting_recordsNothing() {
            service.clearDistanceTip(SHOP_ID, ACTOR);

            assertThat(historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_DISTANCE)).isEmpty();
        }

        @Test
        @DisplayName("공휴일 팁은 0원(삭제)도 UPDATE 한 행으로 남긴다 — 같은 저장 버튼이 두 종류로 갈리지 않는다")
        void changeHolidayTip_recordsUpdateEvenWhenCleared() {
            service.changeHolidayTip(SHOP_ID, 2000, ACTOR);
            service.changeHolidayTip(SHOP_ID, 0, ACTOR);

            List<ShopChangeHistory> histories =
                historyRepository.savedOf(ShopChangeType.DELIVERY_TIP_HOLIDAY);
            assertThat(histories).hasSize(2);
            assertThat(histories).extracting(ShopChangeHistory::getActionType)
                .containsExactly(ShopChangeActionType.UPDATE, ShopChangeActionType.UPDATE);
            assertThat(histories.get(0).getNewValue()).isEqualTo("공휴일: +2,000원");
            assertThat(histories.get(1).getPreviousValue()).isEqualTo("공휴일: +2,000원");
            assertThat(histories.get(1).getNewValue()).isEqualTo("미설정");
        }
    }

    private void registerDeliveryArea(Long adminDongId) {
        adminDongRepository.add(adminDongId);
        areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(adminDongId)));
    }

    private static final class ShopDeliveryTipRepositoryFake implements ShopDeliveryTipRepository {

        private final Map<Long, ShopDeliveryTipSetting> settings = new LinkedHashMap<>();
        private final List<ShopDeliveryTipTier> tiers = new ArrayList<>();
        private final List<ShopDeliveryTipRegion> regionTips = new ArrayList<>();
        private final List<ShopDeliveryTipSchedule> scheduleTips = new ArrayList<>();
        private final Map<Long, ShopDeliveryTipHoliday> holidayTips = new LinkedHashMap<>();

        private long sequence = 0L;
        private int holidayTipDeleteCount = 0;

        @Override
        public Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId) {
            return Optional.ofNullable(settings.get(shopId.value()));
        }

        @Override
        public ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting) {
            settings.put(setting.getShopId().value(), setting);
            return setting;
        }

        @Override
        public List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId) {
            return tiers.stream().filter(tier -> tier.getShopId().equals(shopId)).toList();
        }

        @Override
        public List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> newTiers) {
            List<ShopDeliveryTipTier> saved = new ArrayList<>(newTiers.size());
            for (ShopDeliveryTipTier tier : newTiers) {
                saved.add(ShopDeliveryTipTier.reconstitute(
                    ++sequence, tier.getShopId(), tier.getTierOrder(), tier.getMinOrderAmount(), tier.getTipAmount()
                ));
            }
            tiers.addAll(saved);
            return saved;
        }

        @Override
        public void deleteTiersByShopId(ShopId shopId) {
            tiers.removeIf(tier -> tier.getShopId().equals(shopId));
        }

        @Override
        public List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId) {
            return regionTips.stream().filter(regionTip -> regionTip.getShopId().equals(shopId)).toList();
        }

        @Override
        public long countRegionTipsByShopId(ShopId shopId) {
            return findRegionTipsByShopId(shopId).size();
        }

        @Override
        public List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> newRegionTips) {
            List<ShopDeliveryTipRegion> saved = new ArrayList<>(newRegionTips.size());
            for (ShopDeliveryTipRegion regionTip : newRegionTips) {
                saved.add(ShopDeliveryTipRegion.reconstitute(
                    ++sequence, regionTip.getShopId(), regionTip.getAdminDongId(), regionTip.getTipAmount()
                ));
            }
            regionTips.addAll(saved);
            return saved;
        }

        @Override
        public void deleteRegionTipsByShopId(ShopId shopId) {
            regionTips.removeIf(regionTip -> regionTip.getShopId().equals(shopId));
        }

        @Override
        public List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId) {
            return scheduleTips.stream().filter(scheduleTip -> scheduleTip.getShopId().equals(shopId)).toList();
        }

        @Override
        public List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> newScheduleTips) {
            List<ShopDeliveryTipSchedule> saved = new ArrayList<>(newScheduleTips.size());
            for (ShopDeliveryTipSchedule scheduleTip : newScheduleTips) {
                saved.add(ShopDeliveryTipSchedule.reconstitute(
                    ++sequence,
                    scheduleTip.getShopId(),
                    scheduleTip.getDayType(),
                    scheduleTip.getStartTime(),
                    scheduleTip.getEndTime(),
                    scheduleTip.getTipAmount()
                ));
            }
            scheduleTips.addAll(saved);
            return saved;
        }

        @Override
        public void deleteScheduleTipsByShopId(ShopId shopId) {
            scheduleTips.removeIf(scheduleTip -> scheduleTip.getShopId().equals(shopId));
        }

        @Override
        public Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId) {
            return Optional.ofNullable(holidayTips.get(shopId.value()));
        }

        @Override
        public ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip) {
            ShopDeliveryTipHoliday saved = ShopDeliveryTipHoliday.reconstitute(
                holidayTip.getId() != null ? holidayTip.getId() : ++sequence,
                holidayTip.getShopId(),
                holidayTip.getTipAmount()
            );
            holidayTips.put(saved.getShopId().value(), saved);
            return saved;
        }

        @Override
        public void deleteHolidayTipByShopId(ShopId shopId) {
            holidayTipDeleteCount++;
            holidayTips.remove(shopId.value());
        }
    }

    private static final class ShopDeliveryAreaRepositoryFake implements ShopDeliveryAreaRepository {

        private final Map<Long, ShopDeliveryArea> areas = new LinkedHashMap<>();
        private long sequence = 0L;

        @Override
        public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
            return areas.values().stream().filter(area -> area.getShopId().equals(shopId)).toList();
        }

        @Override
        public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
            return Optional.ofNullable(areas.get(deliveryAreaId));
        }

        @Override
        public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return areas.values().stream()
                .anyMatch(area -> area.getShopId().equals(shopId) && area.getAdminDongId().equals(adminDongId));
        }

        @Override
        public long countByShopId(ShopId shopId) {
            return findByShopId(shopId).size();
        }

        @Override
        public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
            long id = ++sequence;
            ShopDeliveryArea saved = ShopDeliveryArea.reconstitute(
                id, shopDeliveryArea.getShopId(), shopDeliveryArea.getAdminDongId(), shopDeliveryArea.getSource()
            );
            areas.put(id, saved);
            return saved;
        }

        @Override
        public List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas) {
            return shopDeliveryAreas.stream().map(this::save).toList();
        }

        @Override
        public List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            return findByShopId(shopId).stream()
                .filter(area -> area.getSource() == source)
                .toList();
        }

        @Override
        public void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            findByShopIdAndSource(shopId, source).forEach(area -> areas.remove(area.getId()));
        }

        @Override
        public Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId) {
            return findByShopId(shopId).stream()
                .map(ShopDeliveryArea::getAdminDongId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public void deleteById(Long deliveryAreaId) {
            areas.remove(deliveryAreaId);
        }
    }

    private static final class AdminDongRepositoryFake implements AdminDongRepository {

        @Override
        public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
            // 이 테스트들은 조회 경로만 검증한다. 동기화가 불리면 테스트가 잘못 짜인 것이다.
            throw new UnsupportedOperationException("동기화는 이 테스트의 대상이 아닙니다.");
        }

        private final Map<Long, AdminDong> adminDongs = new LinkedHashMap<>();

        void add(Long adminDongId) {
            adminDongs.put(
                adminDongId,
                AdminDong.reconstitute(adminDongId, "1168053100", "서울특별시", "강남구", "역삼1동", true, null, List.of())
            );
        }

        @Override
        public Optional<AdminDong> findById(AdminDongId adminDongId) {
            return Optional.ofNullable(adminDongs.get(adminDongId.value()));
        }

        @Override
        public boolean existsById(AdminDongId adminDongId) {
            return adminDongs.containsKey(adminDongId.value());
        }

        @Override
        public List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox) {
            return adminDongs.values().stream()
                .filter(AdminDong::hasCenter)
                .filter(adminDong -> boundingBox.contains(adminDong.getCenter()))
                .toList();
        }

        @Override
        public List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .map(adminDongId -> adminDongs.get(adminDongId.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }

        @Override
        public Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .filter(adminDongId -> adminDongs.containsKey(adminDongId.value()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
            return adminDongs.values().stream()
                .filter(adminDong -> adminDong.getSidoName().equals(sidoName)
                    && adminDong.getSigunguName().equals(sigunguName)
                    && adminDong.getDongName().equals(dongName))
                .findFirst();
        }
    }
}
