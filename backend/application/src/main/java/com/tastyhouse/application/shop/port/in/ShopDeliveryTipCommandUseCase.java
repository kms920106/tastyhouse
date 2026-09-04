package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 배달팁(구간·거리·지역·시간·공휴일) 쓰기 인바운드 포트.
 *
 * <p>거리별과 지역별은 상호 배타 불변식을 공유하고 다섯 파트를 한 컨트롤러가 소비하므로, per-operation
 * 분해 없이 도메인 단위 인터페이스 1개로 둔다(메서드 7개 = 분해 기준 초과 아님).
 */
@CeoApp
public interface ShopDeliveryTipCommandUseCase {

    void updateTiers(ShopDeliveryTipTiersUpdateCommand command);

    void updateDistanceTip(ShopDeliveryTipDistanceUpdateCommand command);

    void removeDistanceTip(ShopDeliveryTipDistanceRemoveCommand command);

    void updateRegionTips(ShopDeliveryTipRegionsUpdateCommand command);

    void removeRegionTips(ShopDeliveryTipRegionsRemoveCommand command);

    void updateScheduleTips(ShopDeliveryTipSchedulesUpdateCommand command);

    void updateHolidayTip(ShopDeliveryTipHolidayUpdateCommand command);
}
