package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductExposureViewResult;
import com.tastyhouse.application.product.port.in.ProductExposureQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.service.ProductExposureResult;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.ProductExposurePeriodResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductExposureQueryService implements ProductExposureQueryUseCase {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final ProductExposureService productExposureService;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureQueryService(
        ProductExposureService productExposureService,
        PublicHolidayCalendar publicHolidayCalendar,
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductExposureViewResult getExposure(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductExposurePeriodResult period = productOwnerQueryPort.findExposurePeriod(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!period.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductId targetProductId = ProductId.of(productId);
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        LocalDate today = now.toLocalDate();
        ProductExposureResult result = productExposureService.evaluate(
            targetProductId,
            now,
            publicHolidayCalendar.isPublicHoliday(today),
            publicHolidayCalendar.isPublicHoliday(today.minusDays(1))
        );

        List<ProductExposureHour> hours = productExposureService.findHours(targetProductId);

        return new ProductExposureViewResult(
            period.startDate(),
            period.endDate(),
            hours.stream()
                .map(hour -> new ProductExposureViewResult.Hour(
                    hour.getDayType().name(),
                    hour.getStartTime(),
                    hour.getEndTime()
                ))
                .toList(),
            result.exposed(),
            result.hiddenReason()
        );
    }

}
