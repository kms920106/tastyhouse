package com.tastyhouse.application.product.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductHiddenReason;

/**
 * 메뉴 노출 설정 현황 — 노출 기간·시간대·현재 노출 여부와 숨김 사유.
 *
 * <p><b>챕터 09</b>에서 신설. 노출 여부 판정은 도메인 서비스({@code ProductExposureService})가
 * <b>현재 시각과 공휴일 여부</b>를 받아 수행하고 시간대 목록도 그 서비스가 돌려주므로, 조립이
 * application에 남아야 한다 — 표현 계약이 시계를 읽으면 응답이 시점에 의존하는 순수하지 않은 함수가
 * 된다.
 *
 * <p>{@code hours}는 도메인 타입 {@code ProductExposureHour}를 그대로 담지 않고 요일·시각으로 강등한
 * {@link Hour}로 나른다 — api 모듈은 도메인 모델을 알 수 없다.
 */
public record ProductExposureViewResult(
    LocalDate startDate,
    LocalDate endDate,
    List<Hour> hours,
    boolean exposed,
    ProductHiddenReason hiddenReason
) {

    /** 노출 시간대 한 칸. */
    public record Hour(
        String dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
    }
}
