package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

public interface ShopBusinessHourLoadPort {

    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    Optional<ShopBusinessHour> findBusinessHourById(Long id);

    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    Optional<ShopBreakTime> findBreakTimeById(Long id);

    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    Optional<ShopClosedDay> findClosedDayById(Long id);
}
