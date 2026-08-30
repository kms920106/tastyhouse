package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달팁 시간대별 한 줄 command.
 *
 * <p>과거 서비스가 {@code ShopDeliveryTipScheduleItemRequest}를 그대로 받던 자리를 대체한다(챕터 02 §5).
 *
 * <p>{@code dayType}은 경계 타입 {@code String}으로 담고 {@code DayType.from(...)} 승격은
 * 서비스 내부에서 한다. {@code startTime}·{@code endTime}은 둘 다 {@code LocalTime}이라
 * 순서가 어긋나도 컴파일되므로 조립 시 이름으로 짚어 넣는다.
 */
public record ShopDeliveryTipScheduleCommand(
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    Integer tipAmount
) {
    public ShopDeliveryTipScheduleCommand {
        if (dayType == null || startTime == null || endTime == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipScheduleCommand of(
        String dayType,
        LocalTime startTime,
        LocalTime endTime,
        Integer tipAmount
    ) {
        return new ShopDeliveryTipScheduleCommand(
            dayType,
            startTime,
            endTime,
            tipAmount
        );
    }
}
