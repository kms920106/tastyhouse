package com.tastyhouse.application.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOptionType;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.model.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.model.ShopNextOpenTimeContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutCommand;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionTargetCommand;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.out.write.ShopDetailLoadPort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionSoldOutService implements ProductOptionSoldOutUseCase {

    private static final long FALLBACK_SOLD_OUT_HOURS = 24L;

    private static final long HOLIDAY_LOOKUP_DAYS = 7L;

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopNextOpenTimeCalculator shopNextOpenTimeCalculator;
    private final ShopDetailLoadPort shopDetailLoadPort;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionSoldOutService(
        ProductAvailabilityService productAvailabilityService,
        ShopNextOpenTimeCalculator shopNextOpenTimeCalculator,
        ShopDetailLoadPort shopDetailLoadPort,
        PublicHolidayCalendar publicHolidayCalendar,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopNextOpenTimeCalculator = shopNextOpenTimeCalculator;
        this.shopDetailLoadPort = shopDetailLoadPort;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView markOptionsSoldOut(ProductOptionSoldOutCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolved = resolveSoldOutUntil(shopId, soldOutUntil, now);

        return toChangeView(productAvailabilityService.markOptionsSoldOut(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            resolved, now));
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
            shopDetailLoadPort.findBusinessHoursByShopId(shopId),
            shopDetailLoadPort.findClosedDaysByShopId(shopId),
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

    private List<ProductOptionId> toOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.NORMAL).stream()
            .map(ProductOptionId::of)
            .toList();
    }

    private List<ProductCommonOptionId> toCommonOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.COMMON).stream()
            .map(ProductCommonOptionId::of)
            .toList();
    }

    private List<Long> filterByType(List<Long> optionIds, List<String> optionTypes, ProductOptionType wanted) {
        if (optionIds == null || optionIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        List<Long> filtered = new ArrayList<>();
        for (int i = 0; i < optionIds.size(); i++) {
            if (ProductOptionType.from(optionTypes.get(i)) == wanted) {
                filtered.add(optionIds.get(i));
            }
        }
        return filtered;
    }
}
