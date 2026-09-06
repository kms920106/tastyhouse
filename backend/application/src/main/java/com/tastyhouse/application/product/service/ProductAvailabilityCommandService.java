package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.product.port.in.ProductHideCommand;
import com.tastyhouse.application.product.port.in.ProductHideUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionHideCommand;
import com.tastyhouse.application.product.port.in.ProductOptionHideUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutCommand;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUntilChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUntilChangeUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionSoldOutUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionTargetCommand;
import com.tastyhouse.application.product.port.in.ProductReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductReleaseUseCase;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeUseCase;
import com.tastyhouse.application.product.port.in.ProductSoldOutOwnerUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.product.model.ProductOptionType;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.service.ProductAvailabilityChangeResult;
import com.tastyhouse.domain.product.service.ProductAvailabilityService;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeContext;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductAvailabilityCommandService implements ProductSoldOutOwnerUseCase, ProductHideUseCase, ProductReleaseUseCase, ProductSoldOutUntilChangeUseCase, ProductOptionSoldOutUseCase, ProductOptionHideUseCase, ProductOptionReleaseUseCase, ProductOptionSoldOutUntilChangeUseCase {

    private static final long FALLBACK_SOLD_OUT_HOURS = 24L;

    private static final long HOLIDAY_LOOKUP_DAYS = 7L;

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopNextOpenTimeCalculator shopNextOpenTimeCalculator;
    private final ShopDetailRepository shopDetailRepository;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductAvailabilityCommandService(
        ProductAvailabilityService productAvailabilityService,
        ShopNextOpenTimeCalculator shopNextOpenTimeCalculator,
        ShopDetailRepository shopDetailRepository,
        PublicHolidayCalendar publicHolidayCalendar,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopNextOpenTimeCalculator = shopNextOpenTimeCalculator;
        this.shopDetailRepository = shopDetailRepository;
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

    @Override
    public ProductAvailabilityChangeView hideProducts(ProductHideCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.hideProducts(ShopId.of(shopId), toProductIds(productIds)));
    }

    @Override
    public ProductAvailabilityChangeView releaseProducts(ProductReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.releaseProducts(
            ShopId.of(shopId), toProductIds(productIds), ReleaseTarget.from(target)));
    }

    @Override
    public ProductAvailabilityChangeView changeProductsSoldOutUntil(ProductSoldOutUntilChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.changeProductsSoldOutUntil(
            ShopId.of(shopId), toProductIds(productIds), soldOutUntil, LocalDateTime.now()));
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

    @Override
    public ProductAvailabilityChangeView hideOptions(ProductOptionHideCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.hideOptions(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes)));
    }

    @Override
    public ProductAvailabilityChangeView releaseOptions(ProductOptionReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.releaseOptions(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            ReleaseTarget.from(target)));
    }

    @Override
    public ProductAvailabilityChangeView changeOptionsSoldOutUntil(ProductOptionSoldOutUntilChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.changeOptionsSoldOutUntil(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            soldOutUntil, LocalDateTime.now()));
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
            shopDetailRepository.findBusinessHoursByShopId(shopId),
            shopDetailRepository.findClosedDaysByShopId(shopId),
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
                    failure.errorCode()
                ))
                .toList()
        );
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
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
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
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
