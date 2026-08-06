package com.tastyhouse.domain.shop.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
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
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 ceo-api의
 * {@code ShopDeliveryTipCommandService}가 선언한다. 도메인 모델이 POJO라 더티 체킹이 없으므로
 * 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopDeliveryTipService {

    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;

    public ShopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository
    ) {
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
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
     */
    public List<ShopDeliveryTipTier> replaceTiers(ShopId shopId, List<ShopDeliveryTipTierSpec> specs) {
        if (specs == null || specs.isEmpty() || specs.size() > DeliveryTipPolicy.TIER_MAX_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED,
                ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED.getDefaultMessage()
                    + " 입력 구간 수: " + (specs == null ? 0 : specs.size()));
        }

        List<ShopDeliveryTipTierSpec> sorted = specs.stream()
            .sorted(Comparator.comparingInt(ShopDeliveryTipTierSpec::minOrderAmount))
            .toList();

        validateTierMonotonicity(sorted);

        shopDeliveryTipRepository.deleteTiersByShopId(shopId);

        List<ShopDeliveryTipTier> tiers = new ArrayList<>(sorted.size());
        for (int tierOrder = 0; tierOrder < sorted.size(); tierOrder++) {
            ShopDeliveryTipTierSpec spec = sorted.get(tierOrder);
            tiers.add(ShopDeliveryTipTier.of(shopId, tierOrder, spec.minOrderAmount(), spec.tipAmount()));
        }
        return shopDeliveryTipRepository.saveTiers(tiers);
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
        int surchargeAmount
    ) {
        if (shopDeliveryTipRepository.countRegionTipsByShopId(shopId) > 0) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 지역별 배달팁을 모두 삭제한 뒤 거리별을 설정하세요.");
        }

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        setting.changeToDistance(baseDistanceMeters, unit, surchargeAmount);
        return shopDeliveryTipRepository.saveSetting(setting);
    }

    /** 거리별 추가 배달팁을 해제한다(설정 헤더가 없으면 아무것도 하지 않는다). */
    public void clearDistanceTip(ShopId shopId) {
        shopDeliveryTipRepository.findSettingByShopId(shopId).ifPresent(setting -> {
            setting.clearExtraTip();
            shopDeliveryTipRepository.saveSetting(setting);
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
     */
    public List<ShopDeliveryTipRegion> replaceRegionTips(ShopId shopId, List<ShopDeliveryTipRegionSpec> specs) {
        List<ShopDeliveryTipRegionSpec> requested = specs == null ? List.of() : specs;

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        if (setting.usesDistance() && !requested.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 거리별 배달팁을 해제한 뒤 지역별을 설정하세요.");
        }

        List<ShopDeliveryTipRegion> regionTips = buildRegionTips(shopId, requested);

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

        return saved;
    }

    /**
     * 지역별 추가 배달팁을 전부 삭제한다 — 설정 헤더는 {@code NONE}으로 되돌아간다.
     *
     * <p>거리별 설정을 쓰고 있던 가게에는 영향이 없다(거리별과 지역별은 배타라 애초에 공존하지 않는다).
     */
    public void clearRegionTips(ShopId shopId) {
        replaceRegionTips(shopId, List.of());
    }

    /**
     * 시간별 추가 배달팁을 통째로 교체한다.
     *
     * <p>불변식: {@code HOLIDAY} 요일 구분 금지(공휴일은 전용 애그리거트가 담당 — 겹치면 이중 부과),
     * 같은 요일 구분 안에서 시간 구간 겹침 금지. 각 행의 값 검증은
     * {@link ShopDeliveryTipSchedule#of}가 강제한다.
     */
    public List<ShopDeliveryTipSchedule> replaceScheduleTips(ShopId shopId, List<ShopDeliveryTipScheduleSpec> specs) {
        List<ShopDeliveryTipScheduleSpec> requested = specs == null ? List.of() : specs;

        validateScheduleOverlap(requested);

        List<ShopDeliveryTipSchedule> scheduleTips = requested.stream()
            .map(spec -> ShopDeliveryTipSchedule.of(
                shopId, spec.dayType(), spec.startTime(), spec.endTime(), spec.tipAmount()
            ))
            .toList();

        shopDeliveryTipRepository.deleteScheduleTipsByShopId(shopId);
        return shopDeliveryTipRepository.saveScheduleTips(scheduleTips);
    }

    /**
     * 공휴일 추가 배달팁을 설정한다 — <b>0원이면 삭제</b>로 해석한다(미설정과 0원을 구분하지 않는다).
     *
     * @return 저장된 공휴일 배달팁. 0원을 보내 삭제된 경우 {@code null}
     */
    public ShopDeliveryTipHoliday changeHolidayTip(ShopId shopId, int tipAmount) {
        if (tipAmount == 0) {
            shopDeliveryTipRepository.deleteHolidayTipByShopId(shopId);
            return null;
        }

        ShopDeliveryTipHoliday holidayTip = shopDeliveryTipRepository.findHolidayTipByShopId(shopId)
            .map(existing -> {
                existing.changeTipAmount(tipAmount);
                return existing;
            })
            .orElseGet(() -> ShopDeliveryTipHoliday.of(shopId, tipAmount));

        return shopDeliveryTipRepository.saveHolidayTip(holidayTip);
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
