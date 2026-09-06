package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 배달팁 컬렉션 불변식(도메인 서비스).
 *
 * <p>{@code ShopBusinessHourService}와 같은 자리의 서비스이며, 여기에 남는 것은 <b>행 하나만 보고는
 * 판정할 수 없는 규칙</b>뿐이다:
 * <ul>
 *   <li>구간 집합의 개수·정렬·단조성 — "3개 이하 + 주문금액 오름차순 + 팁 내림차순"</li>
 *   <li>거리별↔지역별 상호 배타 — 두 리소스에 걸친 불변식</li>
 *   <li>지역별 팁의 행정동이 가게 배달가능지역에 속하는지 — 다른 애그리거트 컬렉션을 읽어야 판정</li>
 *   <li>같은 요일 시간대 겹침 — 집합 관계</li>
 * </ul>
 * 행 하나의 값 불변식(금액 범위·시각 유효성)은 각 애그리거트의 {@code of}·{@code update}가 강제한다 —
 * 서비스에 두면 팩토리를 직접 부르는 경로(배치·마이그레이션)가 규격을 우회할 수 있기 때문이다.
 *
 * <p><b>모든 컬렉션은 replace-all로 교체한다.</b> 위 규칙들이 집합 전체를 봐야 판정되므로, 행 단위
 * CRUD면 어떤 순서로 조작해도 중간 상태가 규칙을 위반한다({@code ShopBusinessHour}가 개별 CRUD인 것은
 * 요일 간에 이런 관계가 없기 때문이다).
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>(배달 분류 {@code DELIVERY_TIP_TIER}·
 * {@code DELIVERY_TIP_DISTANCE}·{@code DELIVERY_TIP_REGION}·{@code DELIVERY_TIP_SCHEDULE}
 * ·{@code DELIVERY_TIP_HOLIDAY}). 이 서비스는 replace-all을 수행하려고 이미 컬렉션을 <b>삭제 전에</b>
 * 읽을 수 있는 유일한 지점이고, ceo-api의 {@code CommandService}는 CQRS 교차 주입 금지로 QueryDao를
 * 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다.
 *
 * <p><b>replace-all은 컬렉션 1행당 이력을 남기지 않고 저장 1회당 1행만 남긴다.</b> 이 서비스는
 * {@code deleteAll + saveAll}로 교체하므로 PK 기반 diff가 불가능하고, 행 단위로 남기면 이력 목록이
 * "점주가 저장한 횟수"가 아니라 "바뀐 행 수"로 페이징되어 읽을 수 없게 된다. 따라서 변경 전·후 컬렉션
 * 전체를 {@link ShopChangeValueFormatter#snapshot(List)}으로 요약해 한 행에 담는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 ceo-api의
 * {@code ShopDeliveryTipCommandService}가 선언한다. 도메인 모델이 POJO라 더티 체킹이 없으므로
 * 변경 후 명시적으로 {@code save}를 호출한다. 변경 주체({@link ShopChangeActor})는 도메인이 인증을
 * 모르므로 마지막 파라미터로 명시 전달받는다.
 */
public class ShopDeliveryTipService {

    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    /**
     * 구간별 기본 배달팁을 통째로 교체한다.
     *
     * <p>불변식: 1~{@value DeliveryTipPolicy#TIER_MAX_COUNT}개, 주문금액 strict 오름차순,
     * 팁 strict 내림차순(주문금액이 높아질수록 배달팁이 낮아져야 한다 — 배민 가이드 강제 규격).
     * 각 행의 팁 범위({@code 0 이상 5,000 미만})는 {@link ShopDeliveryTipTier#of}가 강제한다.
     *
     * <p>{@code tier_order}는 호출부가 보낸 순서가 아니라 <b>정렬 후 재부여</b>한다 — 그래야 저장된
     * 순서와 금액 정렬이 어긋나지 않는다.
     *
     * <p>변경이력은 저장 1회당 1행이다 — 삭제 전에 기존 구간 전체를 읽어 스냅샷으로 남긴다.
     */
    public List<ShopDeliveryTipTier> replaceTiers(
        ShopId shopId,
        List<ShopDeliveryTipTierSpec> specs,
        ShopChangeActor actor
    ) {
        if (specs == null || specs.isEmpty() || specs.size() > DeliveryTipPolicy.TIER_MAX_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED,
                ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED.getDefaultMessage()
                    + " 입력 구간 수: " + (specs == null ? 0 : specs.size()));
        }

        List<ShopDeliveryTipTierSpec> sorted = specs.stream()
            .sorted(Comparator.comparingInt(ShopDeliveryTipTierSpec::minOrderAmount))
            .toList();

        validateTierMonotonicity(sorted);

        String previousValue = describeTiers(shopDeliveryTipRepository.findTiersByShopId(shopId));

        shopDeliveryTipRepository.deleteTiersByShopId(shopId);

        List<ShopDeliveryTipTier> tiers = new ArrayList<>(sorted.size());
        for (int tierOrder = 0; tierOrder < sorted.size(); tierOrder++) {
            ShopDeliveryTipTierSpec spec = sorted.get(tierOrder);
            tiers.add(ShopDeliveryTipTier.of(shopId, tierOrder, spec.minOrderAmount(), spec.tipAmount()));
        }
        List<ShopDeliveryTipTier> saved = shopDeliveryTipRepository.saveTiers(tiers);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_TIER,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeTiers(saved)
        );
        return saved;
    }

    /**
     * 거리별 추가 배달팁을 설정한다.
     *
     * <p>지역별 팁이 하나라도 있으면 {@link ErrorCode#SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT}(409)로
     * 거절한다 — 지역별을 전부 삭제해야 거리별로 전환할 수 있다(배민 가이드 규격).
     */
    public ShopDeliveryTipSetting changeDistanceTip(
        ShopId shopId,
        int baseDistanceMeters,
        DeliveryTipDistanceUnit unit,
        int surchargeAmount,
        ShopChangeActor actor
    ) {
        if (shopDeliveryTipRepository.countRegionTipsByShopId(shopId) > 0) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 지역별 배달팁을 모두 삭제한 뒤 거리별을 설정하세요.");
        }

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        String previousValue = describeDistanceTip(setting);

        setting.changeToDistance(baseDistanceMeters, unit, surchargeAmount);
        ShopDeliveryTipSetting saved = shopDeliveryTipRepository.saveSetting(setting);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_DISTANCE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeDistanceTip(saved)
        );
        return saved;
    }

    /**
     * 거리별 추가 배달팁을 해제한다(설정 헤더가 없으면 아무것도 하지 않는다).
     *
     * <p>설정 헤더가 없으면 이력도 남기지 않는다 — 애초에 거리별을 쓰지 않던 가게이므로 "해제했다"고
     * 기록하면 일어나지 않은 변경이 이력에 남는다.
     */
    public void clearDistanceTip(ShopId shopId, ShopChangeActor actor) {
        shopDeliveryTipRepository.findSettingByShopId(shopId).ifPresent(setting -> {
            String previousValue = describeDistanceTip(setting);

            setting.clearExtraTip();
            shopDeliveryTipRepository.saveSetting(setting);

            shopChangeHistoryRecorder.record(
                shopId,
                ShopChangeType.DELIVERY_TIP_DISTANCE,
                ShopChangeActionType.DELETE,
                actor,
                previousValue,
                null
            );
        });
    }

    /**
     * 지역별 추가 배달팁을 통째로 교체한다.
     *
     * <p>불변식: 거리별 설정과 배타, 요청 내 행정동 중복 금지, 각 행정동이 <b>가게의 배달가능지역으로
     * 등록되어 있어야</b> 한다. 각 행의 금액 범위는 {@link ShopDeliveryTipRegion#of}가 강제한다.
     *
     * <p>빈 목록을 보내면 전부 삭제되고 설정 헤더가 {@code NONE}으로 돌아간다 — 그래서 "지역별 전부 삭제
     * 후 거리별 설정 가능"이 별도 분기 없이 자동 성립한다.
     *
     * <p>변경이력은 저장 1회당 1행이다 — 삭제 전에 기존 지역별 팁 전체를 읽어 스냅샷으로 남긴다.
     */
    public List<ShopDeliveryTipRegion> replaceRegionTips(
        ShopId shopId,
        List<ShopDeliveryTipRegionSpec> specs,
        ShopChangeActor actor
    ) {
        List<ShopDeliveryTipRegionSpec> requested = specs == null ? List.of() : specs;

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        if (setting.usesDistance() && !requested.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 거리별 배달팁을 해제한 뒤 지역별을 설정하세요.");
        }

        List<ShopDeliveryTipRegion> regionTips = buildRegionTips(shopId, requested);

        String previousValue = describeRegionTips(shopDeliveryTipRepository.findRegionTipsByShopId(shopId));

        shopDeliveryTipRepository.deleteRegionTipsByShopId(shopId);
        List<ShopDeliveryTipRegion> saved = shopDeliveryTipRepository.saveRegionTips(regionTips);

        if (requested.isEmpty()) {
            // 거리별 설정을 쓰고 있던 가게라면 건드리지 않는다 — 지역별을 비우는 요청이 거리별 설정을
            // 조용히 지우면 안 된다. 위 배타성 검증을 통과했다는 것은 이 시점에 지역별 팁이 없었다는
            // 뜻이므로, 거리별 설정은 그대로 두는 것이 요청 의미에 맞다.
            if (!setting.usesDistance()) {
                setting.clearExtraTip();
            }
        } else {
            setting.changeToRegion();
        }
        shopDeliveryTipRepository.saveSetting(setting);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_REGION,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeRegionTips(saved)
        );

        return saved;
    }

    /**
     * 지역별 추가 배달팁을 전부 삭제한다 — 설정 헤더는 {@code NONE}으로 되돌아간다.
     *
     * <p>거리별 설정을 쓰고 있던 가게에는 영향이 없다(거리별과 지역별은 배타라 애초에 공존하지 않는다).
     *
     * <p>이력은 {@link #replaceRegionTips}가 빈 컬렉션으로의 교체로 남긴다 — 전용 {@code DELETE} 행을
     * 따로 만들지 않는 이유는, 이 경로가 빈 배열 PUT과 완전히 같은 연산이라 이력에서도 구분될 이유가 없기
     * 때문이다(구분하면 같은 결과가 두 형태로 기록된다).
     */
    public void clearRegionTips(ShopId shopId, ShopChangeActor actor) {
        replaceRegionTips(shopId, List.of(), actor);
    }

    /**
     * 시간별 추가 배달팁을 통째로 교체한다.
     *
     * <p>불변식: {@code HOLIDAY} 요일 구분 금지(공휴일은 전용 애그리거트가 담당 — 겹치면 이중 부과),
     * 같은 요일 구분 안에서 시간 구간 겹침 금지. 각 행의 값 검증은
     * {@link ShopDeliveryTipSchedule#of}가 강제한다.
     *
     * <p>변경이력은 저장 1회당 1행이다 — 삭제 전에 기존 시간별 팁 전체를 읽어 스냅샷으로 남긴다.
     */
    public List<ShopDeliveryTipSchedule> replaceScheduleTips(
        ShopId shopId,
        List<ShopDeliveryTipScheduleSpec> specs,
        ShopChangeActor actor
    ) {
        List<ShopDeliveryTipScheduleSpec> requested = specs == null ? List.of() : specs;

        validateScheduleOverlap(requested);

        List<ShopDeliveryTipSchedule> scheduleTips = requested.stream()
            .map(spec -> ShopDeliveryTipSchedule.of(
                shopId, spec.dayType(), spec.startTime(), spec.endTime(), spec.tipAmount()
            ))
            .toList();

        String previousValue = describeScheduleTips(shopDeliveryTipRepository.findScheduleTipsByShopId(shopId));

        shopDeliveryTipRepository.deleteScheduleTipsByShopId(shopId);
        List<ShopDeliveryTipSchedule> saved = shopDeliveryTipRepository.saveScheduleTips(scheduleTips);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_SCHEDULE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeScheduleTips(saved)
        );
        return saved;
    }

    /**
     * 공휴일 추가 배달팁을 설정한다 — <b>0원이면 삭제</b>로 해석한다(미설정과 0원을 구분하지 않는다).
     *
     * <p>이력은 0원(삭제)도 {@code UPDATE} 한 행으로 남긴다. 이 엔드포인트는 "공휴일 배달팁 금액"이라는
     * 스칼라 하나를 설정하는 경로이고 0원은 그 스칼라의 유효한 값(미설정)이라, 같은 저장 버튼이 금액에 따라
     * {@code UPDATE}/{@code DELETE}로 갈리면 이력 목록에서 같은 조작이 두 종류로 보인다.
     *
     * @return 저장된 공휴일 배달팁. 0원을 보내 삭제된 경우 {@code null}
     */
    public ShopDeliveryTipHoliday changeHolidayTip(ShopId shopId, int tipAmount, ShopChangeActor actor) {
        String previousValue = describeHolidayTip(
            shopDeliveryTipRepository.findHolidayTipByShopId(shopId).orElse(null)
        );

        if (tipAmount == 0) {
            shopDeliveryTipRepository.deleteHolidayTipByShopId(shopId);

            shopChangeHistoryRecorder.record(
                shopId,
                ShopChangeType.DELIVERY_TIP_HOLIDAY,
                ShopChangeActionType.UPDATE,
                actor,
                previousValue,
                describeHolidayTip(null)
            );
            return null;
        }

        ShopDeliveryTipHoliday holidayTip = shopDeliveryTipRepository.findHolidayTipByShopId(shopId)
            .map(existing -> {
                existing.changeTipAmount(tipAmount);
                return existing;
            })
            .orElseGet(() -> ShopDeliveryTipHoliday.of(shopId, tipAmount));

        ShopDeliveryTipHoliday saved = shopDeliveryTipRepository.saveHolidayTip(holidayTip);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_HOLIDAY,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeHolidayTip(saved)
        );
        return saved;
    }

    /**
     * 설정 헤더를 읽거나, 없으면 추가 배달팁 미사용 상태로 새로 만든다.
     *
     * <p>이 시점에는 저장하지 않는다 — 호출부가 전환을 마친 뒤 한 번만 저장하도록 해서
     * "만들었지만 전환에 실패한" 빈 헤더가 남지 않게 한다.
     */
    private ShopDeliveryTipSetting loadOrCreateSetting(ShopId shopId) {
        return shopDeliveryTipRepository.findSettingByShopId(shopId)
            .orElseGet(() -> ShopDeliveryTipSetting.of(shopId));
    }

    /**
     * 구간별 기본 배달팁 컬렉션 전체를 스냅샷으로 요약한다(행별 예: {@code "10,000원 이상: 3,000원"}).
     *
     * <p>저장된 {@code tierOrder} 순서를 그대로 쓴다 — 정렬 후 재부여된 순서라 주문금액 오름차순과 일치하고,
     * 화면에 보이는 순서와 이력의 순서가 어긋나지 않는다.
     */
    private String describeTiers(List<ShopDeliveryTipTier> tiers) {
        return ShopChangeValueFormatter.snapshot(
            tiers.stream()
                .sorted(Comparator.comparingInt(ShopDeliveryTipTier::getTierOrder))
                .map(tier -> ShopChangeValueFormatter.amount(tier.getMinOrderAmount()) + " 이상: "
                    + ShopChangeValueFormatter.amount(tier.getTipAmount()))
                .toList()
        );
    }

    /**
     * 거리별 추가 배달팁 설정을 한 줄로 요약한다(예: {@code "2.5km까지: 500m당 500원"}).
     *
     * <p>거리별 설정이 아니거나 설정값이 비어 있으면 "미설정"이다 — 지역별을 쓰는 가게에서 거리별로
     * 전환하는 경우, 변경 전 값이 "미설정"이어야 실제 상태와 맞는다.
     */
    private String describeDistanceTip(ShopDeliveryTipSetting setting) {
        if (setting == null || !setting.usesDistance()
            || setting.getBaseDistanceMeters() == null
            || setting.getSurchargeUnit() == null
            || setting.getSurchargeAmount() == null) {
            return ShopChangeValueFormatter.unset();
        }
        return ShopChangeValueFormatter.distanceKm(toKilometers(setting.getBaseDistanceMeters())) + "까지: "
            + setting.getSurchargeUnit().getUnitMeters() + "m당 "
            + ShopChangeValueFormatter.amount(setting.getSurchargeAmount());
    }

    /**
     * 지역별 추가 배달팁 컬렉션 전체를 스냅샷으로 요약한다(행별 예: {@code "역삼동: +1,000원"}).
     *
     * <p>행정동 이름은 이력을 읽는 사람이 식별자로는 어느 동인지 알 수 없으므로 마스터에서 한 번에 조회해
     * 붙인다. 마스터에 없는 식별자(폐지 동 등)는 이름을 못 붙이므로 식별자를 그대로 노출한다 — 이력에서
     * 행을 통째로 빠뜨리면 "그때 무엇이 설정돼 있었는가"가 부정확해진다.
     */
    private String describeRegionTips(List<ShopDeliveryTipRegion> regionTips) {
        Map<Long, String> namesById = adminDongRepository
            .findAllByIds(regionTips.stream().map(ShopDeliveryTipRegion::getAdminDongId).toList())
            .stream()
            .collect(Collectors.toMap(AdminDong::getId, AdminDong::getDongName, (first, second) -> first));

        return ShopChangeValueFormatter.snapshot(
            regionTips.stream()
                .map(regionTip -> {
                    Long adminDongId = regionTip.getAdminDongId().value();
                    String name = namesById.getOrDefault(adminDongId, "행정동 " + adminDongId);
                    return name + ": +" + ShopChangeValueFormatter.amount(regionTip.getTipAmount());
                })
                .toList()
        );
    }

    /**
     * 시간별 추가 배달팁 컬렉션 전체를 스냅샷으로 요약한다(행별 예: {@code "평일 18:00~20:00: +1,500원"}).
     */
    private String describeScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips) {
        return ShopChangeValueFormatter.snapshot(
            scheduleTips.stream()
                .map(scheduleTip -> scheduleTip.getDayType().getDescription() + " "
                    + ShopChangeValueFormatter.timeRange(scheduleTip.getStartTime(), scheduleTip.getEndTime())
                    + ": +" + ShopChangeValueFormatter.amount(scheduleTip.getTipAmount()))
                .toList()
        );
    }

    /**
     * 공휴일 추가 배달팁을 한 줄로 요약한다(예: {@code "공휴일: +2,000원"}). 설정이 없으면 "미설정".
     */
    private String describeHolidayTip(ShopDeliveryTipHoliday holidayTip) {
        if (holidayTip == null) {
            return ShopChangeValueFormatter.unset();
        }
        return "공휴일: +" + ShopChangeValueFormatter.amount(holidayTip.getTipAmount());
    }

    /**
     * 미터를 km로 환산한다. 소수점 아래 표기는 {@code distanceKm}이 정리한다(2.500 → 2.5km).
     */
    private BigDecimal toKilometers(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    /**
     * 주문금액 오름차순으로 정렬된 구간 목록의 단조성을 검증한다 —
     * 주문금액은 strict 오름차순(같은 금액 구간 2개 금지), 팁은 strict 내림차순이어야 한다.
     */
    private void validateTierMonotonicity(List<ShopDeliveryTipTierSpec> sorted) {
        for (int i = 1; i < sorted.size(); i++) {
            ShopDeliveryTipTierSpec previous = sorted.get(i - 1);
            ShopDeliveryTipTierSpec current = sorted.get(i);

            if (current.minOrderAmount() <= previous.minOrderAmount()) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING,
                    ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING.getDefaultMessage()
                        + " 중복 금액: " + current.minOrderAmount() + "원");
            }
            if (current.tipAmount() >= previous.tipAmount()) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING,
                    ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING.getDefaultMessage()
                        + " " + previous.minOrderAmount() + "원 구간 팁: " + previous.tipAmount()
                        + "원, " + current.minOrderAmount() + "원 구간 팁: " + current.tipAmount() + "원");
            }
        }
    }

    /**
     * 요청된 지역별 팁 목록을 검증해 애그리거트로 만든다 — 행정동 중복과 배달가능지역 미포함을 거절한다.
     */
    private List<ShopDeliveryTipRegion> buildRegionTips(ShopId shopId, List<ShopDeliveryTipRegionSpec> specs) {
        Set<Long> seenAdminDongIds = new HashSet<>();
        List<ShopDeliveryTipRegion> regionTips = new ArrayList<>(specs.size());

        for (ShopDeliveryTipRegionSpec spec : specs) {
            if (!seenAdminDongIds.add(spec.adminDongId())) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_REGION_DUPLICATED,
                    ErrorCode.SHOP_DELIVERY_TIP_REGION_DUPLICATED.getDefaultMessage()
                        + " 행정동 ID: " + spec.adminDongId());
            }

            AdminDongId adminDongId = AdminDongId.of(spec.adminDongId());
            if (!adminDongRepository.existsById(adminDongId)) {
                throw new BusinessException(ErrorCode.ADMIN_DONG_NOT_FOUND,
                    ErrorCode.ADMIN_DONG_NOT_FOUND.getDefaultMessage() + " 행정동 ID: " + spec.adminDongId());
            }
            if (!shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA,
                    ErrorCode.SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA.getDefaultMessage()
                        + " 행정동 ID: " + spec.adminDongId());
            }

            regionTips.add(ShopDeliveryTipRegion.of(shopId, adminDongId, spec.tipAmount()));
        }
        return regionTips;
    }

    /**
     * 같은 요일 구분 안에서 시간 구간이 겹치는지 검증한다.
     *
     * <p>자정 넘김 구간은 {@code [start, 24:00)}과 {@code [00:00, end)} 두 조각으로 나눠 판정한다
     * ({@code ShopBusinessHourService}와 동일 기법) — 그러지 않으면 22:00~02:00과 01:00~03:00처럼
     * 실제로 겹치는 쌍을 놓친다.
     *
     * <p>서로 다른 요일 구분끼리는 검사하지 않는다 — 겹쳐도 적용 시점에 <b>구체성 우선으로 하나만</b>
     * 선택되므로({@code ShopDeliveryTipCalculator}) 이중 부과가 발생하지 않고, 오히려 DAILY 기본값 위에
     * 특정 요일을 덧씌우는 정상적인 설정 방식이기 때문이다.
     */
    private void validateScheduleOverlap(List<ShopDeliveryTipScheduleSpec> specs) {
        for (int i = 0; i < specs.size(); i++) {
            for (int j = i + 1; j < specs.size(); j++) {
                ShopDeliveryTipScheduleSpec left = specs.get(i);
                ShopDeliveryTipScheduleSpec right = specs.get(j);

                if (left.dayType() != right.dayType()) {
                    continue;
                }
                if (overlaps(left, right)) {
                    throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP,
                        ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP.getDefaultMessage()
                            + " " + left.dayType() + " " + left.startTime() + "~" + left.endTime()
                            + " / " + right.startTime() + "~" + right.endTime());
                }
            }
        }
    }

    private boolean overlaps(ShopDeliveryTipScheduleSpec left, ShopDeliveryTipScheduleSpec right) {
        for (int[] leftSegment : toSegments(left.startTime(), left.endTime())) {
            for (int[] rightSegment : toSegments(right.startTime(), right.endTime())) {
                if (leftSegment[0] < rightSegment[1] && rightSegment[0] < leftSegment[1]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 시간 구간을 분 단위 반열림 조각으로 나눈다 — 자정을 넘기면 두 조각, 아니면 한 조각이다.
     */
    private List<int[]> toSegments(LocalTime startTime, LocalTime endTime) {
        int start = toMinuteOfDay(startTime);
        int end = toMinuteOfDay(endTime);
        int endOfDay = 24 * 60;

        if (end <= start) {
            return List.of(new int[] {start, endOfDay}, new int[] {0, end});
        }
        return List.of(new int[] {start, end});
    }

    private int toMinuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
