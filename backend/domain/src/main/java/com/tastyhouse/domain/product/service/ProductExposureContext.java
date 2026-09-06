package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;

/**
 * {@link ProductExposureCalculator}의 입력.
 *
 * <p>계산기는 <b>시계도 타임존도 갖지 않는다</b> — 호출부가
 * {@code LocalDateTime.now(ZoneId.of("Asia/Seoul"))}와 공휴일 판정을 이미 끝낸 값으로 넣는다.
 * 그래야 계산기가 Spring·DB 없이 단위 테스트되는 순수 함수로 남는다.
 *
 * <p>입력을 나열하지 않고 record로 묶는 이유는 판정 근거가 늘어나는 방향으로만 자라기 때문이다 —
 * {@code boolean}·{@code LocalDate}가 여러 개라 파라미터 나열은 순서 착오가 컴파일을 통과한다.
 */
public record ProductExposureContext(
    boolean visible,
    LocalDate exposureStartDate,
    LocalDate exposureEndDate,
    List<ProductExposureHour> hours,
    LocalDateTime now,
    boolean publicHoliday,
    boolean previousDayPublicHoliday
) {

    public ProductExposureContext {
        hours = hours == null ? List.of() : List.copyOf(hours);
    }

    public static ProductExposureContext of(
        boolean visible,
        LocalDate exposureStartDate,
        LocalDate exposureEndDate,
        List<ProductExposureHour> hours,
        LocalDateTime now,
        boolean publicHoliday,
        boolean previousDayPublicHoliday
    ) {
        return new ProductExposureContext(
            visible,
            exposureStartDate,
            exposureEndDate,
            hours,
            now,
            publicHoliday,
            previousDayPublicHoliday
        );
    }
}
