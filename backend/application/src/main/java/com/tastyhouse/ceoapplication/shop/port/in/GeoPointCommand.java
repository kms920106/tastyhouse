package com.tastyhouse.ceoapplication.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 도형을 이루는 좌표 한 점(중첩 command).
 *
 * <p>요청 record와 마찬가지로 {@code [경도, 위도]} 배열이 아니라 이름 있는 필드로 다룬다 — 순서가
 * 뒤바뀌어도 값이 유효 범위 안이면 조용히 통과하는 사고를 원천 차단하기 위해서다.
 */
public record GeoPointCommand(
    BigDecimal latitude,
    BigDecimal longitude
) {
    public GeoPointCommand {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
