package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

/**
 * 메뉴 노출기간 설정의 조회·조립·저장을 담당한다. 판정 자체는 순수 계산기
 * {@link ProductExposureCalculator}가 하고, 이 서비스는 그 입력을 모아 넘긴다.
 *
 * <p>요일·시간대는 <b>replace-all</b>로 교체한다 — "요일 묶음과 개별 요일 혼용 금지"가 집합 전체를
 * 봐야 판정되는 규칙이라, 행 단위로 열면 중간 상태가 반드시 규칙을 위반한다.
 */
public class ProductExposureService {

    /** 요일 묶음 상수 — 개별 요일과 함께 쓸 수 없다. */
    private static final Set<DayType> GROUP_DAY_TYPES =
        Set.of(DayType.DAILY, DayType.WEEKDAY, DayType.WEEKEND, DayType.HOLIDAY);

    private final ProductRepository productRepository;
    private final ProductExposureHourRepository productExposureHourRepository;
    private final ProductExposureCalculator productExposureCalculator;

    public ProductExposureService(
        ProductRepository productRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator
    ) {
        this.productRepository = productRepository;
        this.productExposureHourRepository = productExposureHourRepository;
        this.productExposureCalculator = productExposureCalculator;
    }

    /**
     * 노출기간(기간 + 요일·시간대)을 통째로 교체한다.
     *
     * <p>{@code hours}가 비어 있으면 요일·시간 제약이 없는 상태가 된다(기간 축만 남는다).
     */
    public void replaceSchedule(
        ProductId productId,
        LocalDate startDate,
        LocalDate endDate,
        List<ProductExposureHour> hours
    ) {
        Product product = loadProduct(productId);
        validateDayTypes(hours);

        // 기간 검증(endDate < startDate)은 도메인 모델이 소유한다.
        product.changeExposurePeriod(startDate, endDate);
        productRepository.save(product);

        productExposureHourRepository.deleteAllByProductId(productId);
        if (hours != null && !hours.isEmpty()) {
            productExposureHourRepository.saveAll(hours);
        }
    }

    /**
     * 스케줄을 해제해 상시 노출로 되돌린다. 기간·요일·시간대를 모두 비운다.
     *
     * <p>{@code visible}은 건드리지 않는다 — 숨김은 점주의 별개 의사이므로 스케줄 해제가
     * 숨긴 메뉴를 되살리면 안 된다.
     */
    public void clearSchedule(ProductId productId) {
        Product product = loadProduct(productId);
        product.changeExposurePeriod(null, null);
        productRepository.save(product);
        productExposureHourRepository.deleteAllByProductId(productId);
    }

    /** 지금 이 메뉴가 노출 중인지와 그 사유를 판정한다. */
    public ProductExposureResult evaluate(ProductId productId, LocalDateTime now, boolean publicHoliday,
        boolean previousDayPublicHoliday) {
        Product product = loadProduct(productId);
        return productExposureCalculator.calculate(ProductExposureContext.of(
            product.isVisible(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            productExposureHourRepository.findAllByProductId(productId),
            now,
            publicHoliday,
            previousDayPublicHoliday
        ));
    }

    /** 이 메뉴의 요일·시간대 설정을 반환한다. 빈 목록이면 요일·시간 제약이 없다는 뜻이다. */
    public List<ProductExposureHour> findHours(ProductId productId) {
        return productExposureHourRepository.findAllByProductId(productId);
    }

    /**
     * 요일 묶음(DAILY/WEEKDAY/WEEKEND/HOLIDAY)과 개별 요일(MONDAY..SUNDAY)의 혼용을 금지한다.
     *
     * <p>혼용을 허용하면 "평일 11~14시"와 "월요일 18~20시"가 동시에 있을 때 월요일 12시가 노출인지
     * 아닌지가 <b>SQL 술어의 OR 매칭</b>과 <b>계산기의 구체성 우선</b> 중 어느 규칙을 따르느냐로 갈린다.
     * 애초에 그런 조합을 저장하지 못하게 하면 두 구현이 갈릴 여지가 없다.
     */
    private void validateDayTypes(List<ProductExposureHour> hours) {
        if (hours == null || hours.isEmpty()) {
            return;
        }
        Set<DayType> dayTypes = new LinkedHashSet<>();
        List<DayType> ordered = new ArrayList<>();
        for (ProductExposureHour hour : hours) {
            DayType dayType = Objects.requireNonNull(hour.getDayType(), "dayType은 필수입니다.");
            if (!dayTypes.add(dayType)) {
                // 같은 요일이 두 번 오면 유니크 제약(product_id, day_type) 위반이므로 미리 막는다.
                throw new BusinessException(ErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
            }
            ordered.add(dayType);
        }

        boolean hasGroup = ordered.stream().anyMatch(GROUP_DAY_TYPES::contains);
        boolean hasSpecific = ordered.stream().anyMatch(dayType -> !GROUP_DAY_TYPES.contains(dayType));
        if (hasGroup && hasSpecific) {
            throw new BusinessException(ErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
        }
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
