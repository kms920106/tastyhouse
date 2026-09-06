package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductExposureClearCommand;
import com.tastyhouse.application.product.port.in.ProductExposureCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductExposureHourCommand;
import com.tastyhouse.application.product.port.in.ProductExposureReplaceCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductExposureCommandService implements ProductExposureCommandUseCase {

    private final ProductExposureService productExposureService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureCommandService(
        ProductExposureService productExposureService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.productRepository = productRepository;
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

    @Override
    public void clearExposure(ProductExposureClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productExposureService.clearSchedule(ProductId.of(productId));
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
        List<Product> found = productRepository.findAllByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
