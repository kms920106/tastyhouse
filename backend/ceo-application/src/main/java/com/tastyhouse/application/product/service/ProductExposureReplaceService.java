package com.tastyhouse.application.product.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductExposureHourCommand;
import com.tastyhouse.application.product.port.in.ProductExposureReplaceCommand;
import com.tastyhouse.application.product.port.in.ProductExposureReplaceUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductExposureReplaceService implements ProductExposureReplaceUseCase {

    private final ProductExposureService productExposureService;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureReplaceService(
        ProductExposureService productExposureService,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void replaceExposure(ProductExposureReplaceCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();
        List<String> dayTypes = command.hours().stream().map(ProductExposureHourCommand::dayType).toList();
        List<LocalTime> startTimes = command.hours().stream().map(ProductExposureHourCommand::startTime).toList();
        List<LocalTime> endTimes = command.hours().stream().map(ProductExposureHourCommand::endTime).toList();

        requireOwnedProduct(ceoId, shopId, productId);

        ProductId targetProductId = ProductId.of(productId);
        List<ProductExposureHour> hours = toExposureHours(targetProductId, dayTypes, startTimes, endTimes);
        productExposureService.replaceSchedule(targetProductId, startDate, endDate, hours);
    }

    private List<ProductExposureHour> toExposureHours(
        ProductId productId,
        List<String> dayTypes,
        List<LocalTime> startTimes,
        List<LocalTime> endTimes
    ) {
        return IntStream.range(0, dayTypes.size())
            .mapToObj(index -> ProductExposureHour.of(
                productId,
                DayType.from(dayTypes.get(index)),
                startTimes.get(index),
                endTimes.get(index)
            ))
            .toList();
    }

    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productLoadPort.findAllActiveByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
