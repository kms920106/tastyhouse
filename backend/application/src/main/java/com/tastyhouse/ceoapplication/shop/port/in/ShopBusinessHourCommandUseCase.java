package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 운영시간·브레이크타임 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopBusinessHourCommandService})을 알지 않는다.
 */
public interface ShopBusinessHourCommandUseCase {

    Long createBusinessHour(ShopBusinessHourOwnerCreateCommand command);

    void updateBusinessHour(ShopBusinessHourOwnerUpdateCommand command);

    void deleteBusinessHour(ShopBusinessHourOwnerDeleteCommand command);

    Long createBreakTime(ShopBreakTimeOwnerCreateCommand command);

    void updateBreakTime(ShopBreakTimeOwnerUpdateCommand command);

    void deleteBreakTime(ShopBreakTimeOwnerDeleteCommand command);
}
