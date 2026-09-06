package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class OrderProductValidationService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionGroupLinkRepository productOptionGroupLinkRepository;
    private final ProductExposureHourRepository productExposureHourRepository;
    private final ProductExposureCalculator productExposureCalculator;
    private final CupDepositPolicy cupDepositPolicy;

    public OrderProductValidationService(
        ProductRepository productRepository,
        ProductPriceRepository productPriceRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.productOptionGroupLinkRepository = productOptionGroupLinkRepository;
        this.productExposureHourRepository = productExposureHourRepository;
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
        Product product = productRepository.findById(ProductId.of(line.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + line.productId()));

        if (!isExposed(product, now)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_NOT_AVAILABLE,
                ErrorCode.ORDER_PRODUCT_NOT_AVAILABLE.getDefaultMessage() + ": " + product.getName());
        }

        if (product.isSoldOut()) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
        }

        UploadedFileId representativeImageFileId =
            productImageRepository.findRepresentativeImageFileId(product.getProductId());

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
        List<ProductPrice> prices = productPriceRepository.findAllByProductId(product.getProductId());
        if (prices.isEmpty()) {
            return null;
        }
        if (line.priceId() == null) {
            return prices.getFirst();
        }
        return prices.stream()
            .filter(price -> line.priceId().equals(price.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));
    }

    private boolean isExposed(Product product, LocalDateTime now) {
        ProductExposureContext context = ProductExposureContext.of(
            product.isVisible(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            productExposureHourRepository.findAllByProductId(product.getProductId()),
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
            ProductOptionGroup optionGroup = productOptionGroupRepository
                .findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            if (!productOptionGroupLinkRepository.existsByProductIdAndOptionGroupId(productId, groupId)) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND);
            }

            ProductOption option = productOptionRepository
                .findById(ProductOptionId.of(selected.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

            if (!option.getOptionGroupId().equals(groupId)) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND);
            }

            if (!option.isVisible()) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND);
            }
            if (option.isSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + option.getName());
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
        for (ProductOptionGroupLink link : productOptionGroupLinkRepository.findAllByProductId(productId)) {
            Long groupId = link.getOptionGroupId().value();
            ProductOptionGroup group = selectedGroups.get(groupId);
            if (group == null) {
                group = productOptionGroupRepository.findById(ProductOptionGroupId.of(groupId)).orElse(null);
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
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }

        if (selectedCount == 0) {
            return;
        }

        Integer minSelect = group.getMinSelect();
        if (minSelect != null && selectedCount < minSelect) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }

        Integer maxSelect = group.getMaxSelect();
        if (maxSelect != null && selectedCount > maxSelect) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }
    }
}
