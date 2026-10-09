package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

public interface ShopBusinessHourSavePort {

    ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour);

    void deleteBusinessHourById(Long id);

    ShopBreakTime saveBreakTime(ShopBreakTime breakTime);

    void deleteBreakTimeById(Long id);

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);
}
