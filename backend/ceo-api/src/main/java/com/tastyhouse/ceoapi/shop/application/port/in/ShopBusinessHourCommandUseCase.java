package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 운영시간·브레이크타임 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopBusinessHourCommandService})을 알지 않는다.
 */
public interface ShopBusinessHourCommandUseCase {

    Long createBusinessHour(ShopBusinessHourCreateCommand command);

    void updateBusinessHour(ShopBusinessHourUpdateCommand command);

    void deleteBusinessHour(ShopBusinessHourDeleteCommand command);

    Long createBreakTime(ShopBreakTimeCreateCommand command);

    void updateBreakTime(ShopBreakTimeUpdateCommand command);

    void deleteBreakTime(ShopBreakTimeDeleteCommand command);
}
