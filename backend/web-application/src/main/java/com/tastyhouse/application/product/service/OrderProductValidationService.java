package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.OrderLineOptionSelection;
import com.tastyhouse.domain.product.model.OrderLineSelection;
import com.tastyhouse.domain.product.model.OrderProductOptionSnapshot;
import com.tastyhouse.domain.product.model.OrderProductSnapshot;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureCalculator;
import com.tastyhouse.domain.product.model.ProductExposureContext;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPricePersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class OrderProductValidationService {

    private final ProductPersistencePort productPersistencePort;
    private final ProductPricePersistencePort productPricePersistencePort;
    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort;
    private final ProductExposureHourPersistencePort productExposureHourPersistencePort;
    private final ProductExposureCalculator productExposureCalculator;
    private final CupDepositPolicy cupDepositPolicy;

    public OrderProductValidationService(
        ProductPersistencePort productPersistencePort,
        ProductPricePersistencePort productPricePersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductExposureHourPersistencePort productExposureHourPersistencePort,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productPricePersistencePort = productPricePersistencePort;
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.productOptionGroupLinkPersistencePort = productOptionGroupLinkPersistencePort;
        this.productExposureHourPersistencePort = productExposureHourPersistencePort;
        this.productExposureCalculator = productExposureCalculator;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    public List<OrderProductSnapshot> validate(
        List<OrderLineSelection> lines,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        List<OrderProductSnapshot> snapshots = new ArrayList<>();
        for (OrderLineSelection line : lines) {
            snapshots.add(validateLine(line, orderMethod, now));
        }
        return snapshots;
    }

    private OrderProductSnapshot validateLine(OrderLineSelection line, OrderMethod orderMethod, LocalDateTime now) {
        Product product = productPersistencePort.findById(ProductId.of(line.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_PRODUCT_NOT_FOUND,
                WebErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + line.productId()));

        if (!isExposed(product, now)) {
            throw new ApplicationException(WebErrorCode.ORDER_PRODUCT_NOT_AVAILABLE,
                WebErrorCode.ORDER_PRODUCT_NOT_AVAILABLE.getDefaultMessage() + ": " + product.getName());
        }

        if (product.isSoldOut()) {
            throw new ApplicationException(WebErrorCode.ORDER_PRODUCT_SOLD_OUT,
                WebErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
        }

        UploadedFileId representativeImageFileId =
            productImagePersistencePort.findRepresentativeImageFileId(product.getProductId());

        ProductPrice price = resolvePrice(product, line);

        return new OrderProductSnapshot(
            product.getProductId(),
            price != null ? price.getProductPriceId() : null,
            product.getName(),
            price != null ? price.getPriceName() : null,
            representativeImageFileId,
            line.quantity(),

            price != null ? price.resolvePrice(orderMethod) : product.getOriginalPrice(),
            product.getDiscountPrice(),
            validateOptions(line)
        );
    }

    private ProductPrice resolvePrice(Product product, OrderLineSelection line) {
        List<ProductPrice> prices = productPricePersistencePort.findAllByProductId(product.getProductId());
        if (prices.isEmpty()) {
            return null;
        }
        if (line.priceId() == null) {
            return prices.getFirst();
        }
        return prices.stream()
            .filter(price -> line.priceId().equals(price.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_PRICE_NOT_FOUND));
    }

    private boolean isExposed(Product product, LocalDateTime now) {
        ProductExposureContext context = ProductExposureContext.of(
            product.isVisible(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            productExposureHourPersistencePort.findAllByProductId(product.getProductId()),
            now,
            false,
            false
        );
        return productExposureCalculator.calculate(context).exposed();
    }

    private List<OrderProductOptionSnapshot> validateOptions(OrderLineSelection line) {
        ProductId productId = ProductId.of(line.productId());
        List<OrderProductOptionSnapshot> options = new ArrayList<>();
        Map<Long, ProductOptionGroup> selectedGroups = new LinkedHashMap<>();
        Map<Long, Integer> selectedCountByGroupId = new LinkedHashMap<>();
        for (OrderLineOptionSelection selected : line.selectedOptions()) {
            ProductOptionGroupId groupId = ProductOptionGroupId.of(selected.groupId());
            ProductOptionGroup optionGroup = productOptionGroupPersistencePort
                .findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            if (!productOptionGroupLinkPersistencePort.existsByProductIdAndOptionGroupId(productId, groupId)) {
                throw new ResourceNotFoundException(WebErrorCode.ORDER_OPTION_GROUP_NOT_FOUND);
            }

            ProductOption option = productOptionPersistencePort
                .findById(ProductOptionId.of(selected.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_OPTION_NOT_FOUND));

            if (!option.getOptionGroupId().equals(groupId)) {
                throw new ResourceNotFoundException(WebErrorCode.ORDER_OPTION_NOT_FOUND);
            }

            if (!option.isVisible()) {
                throw new ResourceNotFoundException(WebErrorCode.ORDER_OPTION_NOT_FOUND);
            }
            if (option.isSoldOut()) {
                throw new ApplicationException(WebErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    WebErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + option.getName());
            }

            selectedGroups.putIfAbsent(groupId.value(), optionGroup);
            selectedCountByGroupId.merge(groupId.value(), 1, Integer::sum);

            int depositAmount = optionGroup.isCupDeposit()
                ? cupDepositPolicy.depositAmountOf(option.getCupCount())
                : 0;

            options.add(new OrderProductOptionSnapshot(
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice(),
                optionGroup.getGroupType().name(),
                optionGroup.isCupDeposit() ? option.getCupCount() : null,
                depositAmount,

                option.getPersonalCupDiscountAmount() != null ? option.getPersonalCupDiscountAmount() : 0
            ));
        }

        validateSelectCounts(productId, selectedGroups, selectedCountByGroupId);
        return options;
    }

    private void validateSelectCounts(
        ProductId productId,
        Map<Long, ProductOptionGroup> selectedGroups,
        Map<Long, Integer> selectedCountByGroupId
    ) {
        for (ProductOptionGroupLink link : productOptionGroupLinkPersistencePort.findAllByProductId(productId)) {
            Long groupId = link.getOptionGroupId().value();
            ProductOptionGroup group = selectedGroups.get(groupId);
            if (group == null) {
                group = productOptionGroupPersistencePort.findById(ProductOptionGroupId.of(groupId)).orElse(null);
            }

            if (group == null || !group.isVisible()) {
                continue;
            }

            int selectedCount = selectedCountByGroupId.getOrDefault(groupId, 0);
            validateSelectCount(group, selectedCount);
        }
    }

    private void validateSelectCount(ProductOptionGroup group, int selectedCount) {
        if (group.isRequired() && selectedCount == 0) {
            throw new ApplicationException(WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }

        if (selectedCount == 0) {
            return;
        }

        Integer minSelect = group.getMinSelect();
        if (minSelect != null && selectedCount < minSelect) {
            throw new ApplicationException(WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }

        Integer maxSelect = group.getMaxSelect();
        if (maxSelect != null && selectedCount > maxSelect) {
            throw new ApplicationException(WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                WebErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }
    }
}
