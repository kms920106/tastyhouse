package com.tastyhouse.application.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureCalculator;
import com.tastyhouse.domain.product.model.ProductExposureContext;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.model.ProductExposureResult;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourSavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
public class ProductExposureService {

    private static final Set<DayType> GROUP_DAY_TYPES =
        Set.of(DayType.DAILY, DayType.WEEKDAY, DayType.WEEKEND, DayType.HOLIDAY);

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductExposureHourLoadPort productExposureHourLoadPort;
    private final ProductExposureHourSavePort productExposureHourSavePort;
    private final ProductExposureCalculator productExposureCalculator;

    public ProductExposureService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductExposureHourLoadPort productExposureHourLoadPort,
        ProductExposureHourSavePort productExposureHourSavePort,
        ProductExposureCalculator productExposureCalculator
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productExposureHourLoadPort = productExposureHourLoadPort;
        this.productExposureHourSavePort = productExposureHourSavePort;
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
        productSavePort.save(product);

        productExposureHourSavePort.deleteAllByProductId(productId);
        if (hours != null && !hours.isEmpty()) {
            productExposureHourSavePort.saveAll(hours);
        }
    }

    public void clearSchedule(ProductId productId) {
        Product product = loadProduct(productId);
        product.changeExposurePeriod(null, null);
        productSavePort.save(product);
        productExposureHourSavePort.deleteAllByProductId(productId);
    }

    public ProductExposureResult evaluate(ProductId productId, LocalDateTime now, boolean publicHoliday,
        boolean previousDayPublicHoliday) {
        Product product = loadProduct(productId);
        return productExposureCalculator.calculate(ProductExposureContext.of(
            product.isVisible(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            productExposureHourLoadPort.findAllByProductId(productId),
            now,
            publicHoliday,
            previousDayPublicHoliday
        ));
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
                throw new ApplicationException(CeoErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
            }
            ordered.add(dayType);
        }

        boolean hasGroup = ordered.stream().anyMatch(GROUP_DAY_TYPES::contains);
        boolean hasSpecific = ordered.stream().anyMatch(dayType -> !GROUP_DAY_TYPES.contains(dayType));
        if (hasGroup && hasSpecific) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
        }
    }

    private Product loadProduct(ProductId productId) {
        return productLoadPort.findActiveById(productId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }
}
