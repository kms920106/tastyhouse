package com.tastyhouse.application.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.model.ShopNextOpenTimeContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourLoadPort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductSoldOutOwnerService implements ProductSoldOutOwnerUseCase {

    private static final long FALLBACK_SOLD_OUT_HOURS = 24L;

    private static final long HOLIDAY_LOOKUP_DAYS = 7L;

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopNextOpenTimeCalculator shopNextOpenTimeCalculator;
    private final ShopBusinessHourLoadPort shopBusinessHourLoadPort;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductSoldOutOwnerService(
        ProductAvailabilityService productAvailabilityService,
        ShopNextOpenTimeCalculator shopNextOpenTimeCalculator,
        ShopBusinessHourLoadPort shopBusinessHourLoadPort,
        PublicHolidayCalendar publicHolidayCalendar,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopNextOpenTimeCalculator = shopNextOpenTimeCalculator;
        this.shopBusinessHourLoadPort = shopBusinessHourLoadPort;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView markProductsSoldOut(ProductSoldOutOwnerCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolved = resolveSoldOutUntil(shopId, soldOutUntil, now);

        return toChangeView(productAvailabilityService.markProductsSoldOut(
            ShopId.of(shopId), toProductIds(productIds), resolved, now));
    }

    private LocalDateTime resolveSoldOutUntil(Long shopId, LocalDateTime soldOutUntil, LocalDateTime now) {
        if (soldOutUntil != null) {
            return soldOutUntil;
        }

        LocalDate today = now.toLocalDate();
        Set<LocalDate> publicHolidays =
            publicHolidayCalendar.findBetween(today, today.plusDays(HOLIDAY_LOOKUP_DAYS));

        ShopNextOpenTimeContext context = ShopNextOpenTimeContext.of(
            now,
            shopBusinessHourLoadPort.findBusinessHoursByShopId(shopId),
            shopBusinessHourLoadPort.findClosedDaysByShopId(shopId),
            publicHolidays
        );

        LocalDateTime nextOpenTime = shopNextOpenTimeCalculator.calculate(context);
        return nextOpenTime != null ? nextOpenTime : now.plusHours(FALLBACK_SOLD_OUT_HOURS);
    }

    private ProductAvailabilityChangeView toChangeView(ProductAvailabilityChangeResult result) {
        return new ProductAvailabilityChangeView(
            result.succeeded(),
            result.failed().stream()
                .map(failure -> new ProductAvailabilityChangeView.Failure(
                    failure.id(),
                    failure.name(),
                    failure.errorCode().getCode(),
                    failure.errorCode().getDefaultMessage()
                ))
                .toList()
        );
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
    }
}
