package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 배달팁 5종(설정 헤더·구간·지역별·시간별·공휴일) write 포트.
 *
 * <p>영업시간·휴게시간·정기휴무 3종을 한 인터페이스에 묶은 {@code ShopDetailRepository} 선례대로,
 * 배달팁 5종도 하나에 둔다 — 거리별↔지역별 상호 배타처럼 <b>여러 리소스에 걸친 불변식</b>이 있어
 * 한 트랜잭션·한 포트에서 다뤄야 하기 때문이다.
 *
 * <p>여기 있는 조회는 전부 불변식 검증·상태 전이·주문 접수 시 배달팁 산출에 필요한 것이므로 write 포트
 * 잔류 기준을 만족한다. 점주 설정 화면·고객 상세의 <b>표현용 조회</b>는 infrastructure-module의
 * {@code shop/query/ShopDeliveryTipQueryDao} + {@code ShopDeliveryTip*Result}가 별도로 담당한다
 * (CQRS 교차 주입 금지).
 *
 * <p>컬렉션 3종(구간·지역별·시간별)은 replace-all로 교체하므로 {@code deleteXxxByShopId} +
 * {@code saveXxx(List)} 쌍을 제공한다.
 */
public interface ShopDeliveryTipRepository {

    Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId);

    ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting);

    List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> tiers);

    void deleteTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId);

    /** 지역별 팁 개수 — 거리별 전환 시 배타성 검증에 쓴다(행을 전부 읽지 않으려 count로 둔다). */
    long countRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> regionTips);

    void deleteRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips);

    void deleteScheduleTipsByShopId(ShopId shopId);

    Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId);

    ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip);

    void deleteHolidayTipByShopId(ShopId shopId);
}
