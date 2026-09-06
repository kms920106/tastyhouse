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

public class ProductExposureService {
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

    public void replaceSchedule(
        ProductId productId,
        LocalDate startDate,
        LocalDate endDate,
        List<ProductExposureHour> hours
    ) {
        Product product = loadProduct(productId);
        validateDayTypes(hours);

        product.changeExposurePeriod(startDate, endDate);
        productRepository.save(product);

        productExposureHourRepository.deleteAllByProductId(productId);
        if (hours != null && !hours.isEmpty()) {
            productExposureHourRepository.saveAll(hours);
        }
    }

    public void clearSchedule(ProductId productId) {
        Product product = loadProduct(productId);
        product.changeExposurePeriod(null, null);
        productRepository.save(product);
        productExposureHourRepository.deleteAllByProductId(productId);
    }

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

    public List<ProductExposureHour> findHours(ProductId productId) {
        return productExposureHourRepository.findAllByProductId(productId);
    }

    private void validateDayTypes(List<ProductExposureHour> hours) {
        if (hours == null || hours.isEmpty()) {
            return;
        }
        Set<DayType> dayTypes = new LinkedHashSet<>();
        List<DayType> ordered = new ArrayList<>();
        for (ProductExposureHour hour : hours) {
            DayType dayType = Objects.requireNonNull(hour.getDayType(), "dayType은 필수입니다.");
            if (!dayTypes.add(dayType)) {
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
