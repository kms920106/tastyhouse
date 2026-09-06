package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductAvailabilityService {
    private static final long MIN_SOLD_OUT_MINUTES = 30L;

    private static final long MAX_SOLD_OUT_DAYS = 7L;

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductCommonOptionGroupRepository productCommonOptionGroupRepository;
    private final ProductOptionGroupLinkRepository productOptionGroupLinkRepository;
    private final ProductCommonOptionGroupLinkRepository productCommonOptionGroupLinkRepository;

    public ProductAvailabilityService(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductCommonOptionGroupRepository productCommonOptionGroupRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductCommonOptionGroupLinkRepository productCommonOptionGroupLinkRepository
    ) {
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productCommonOptionRepository = productCommonOptionRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productCommonOptionGroupRepository = productCommonOptionGroupRepository;
        this.productOptionGroupLinkRepository = productOptionGroupLinkRepository;
        this.productCommonOptionGroupLinkRepository = productCommonOptionGroupLinkRepository;
    }

    public void validateSoldOutUntil(LocalDateTime soldOutUntil, LocalDateTime now) {
        if (soldOutUntil == null) {
            return;
        }
        if (soldOutUntil.isBefore(now.plusMinutes(MIN_SOLD_OUT_MINUTES))) {
            throw new BusinessException(ErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_SOON);
        }
        if (soldOutUntil.isAfter(now.plusDays(MAX_SOLD_OUT_DAYS))) {
            throw new BusinessException(ErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_FAR);
        }
    }

    public ProductAvailabilityChangeResult hideProducts(ShopId shopId, List<ProductId> productIds) {
        LoadedProducts loaded = loadProducts(shopId, productIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        List<Product> candidates = loaded.found().stream()
            .filter(Product::isVisible)
            .sorted(Comparator.comparing(Product::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        List<Product> alreadyHidden = loaded.found().stream()
            .filter(product -> !product.isVisible())
            .toList();

        long visibleShortfall =
            Math.max(0, 1 - (productRepository.countVisibleByShopId(shopId) - candidates.size()));
        long representativeTargets = candidates.stream().filter(Product::isRepresentative).count();
        long representativeShortfall =
            Math.max(0, 1 - (productRepository.countVisibleRepresentativeByShopId(shopId) - representativeTargets));

        Map<Long, ProductAvailabilityFailure> rejected = new LinkedHashMap<>();
        rejectFromTail(candidates, rejected, representativeShortfall,
            ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE, Product::isRepresentative);
        rejectFromTail(candidates, rejected, visibleShortfall - rejected.size(),
            ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE, product -> true);

        failed.addAll(rejected.values());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : candidates) {
            if (rejected.containsKey(product.getId())) {
                continue;
            }
            product.deactivate();
            productRepository.save(product);
            succeeded.add(product.getId());
        }

        alreadyHidden.forEach(product -> succeeded.add(product.getId()));

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    public ProductAvailabilityChangeResult markProductsSoldOut(
        ShopId shopId,
        List<ProductId> productIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        validateSoldOutUntil(soldOutUntil, now);
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (soldOutUntil != null) {
                product.markSoldOut(soldOutUntil);
            } else {
                product.markSoldOut();
            }
            productRepository.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult releaseProductsSoldOut(ShopId shopId, List<ProductId> productIds) {
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            product.releaseSoldOut();
            productRepository.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult releaseProducts(
        ShopId shopId,
        List<ProductId> productIds,
        ReleaseTarget target
    ) {
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (target == ReleaseTarget.SOLD_OUT || target == ReleaseTarget.ALL) {
                product.releaseSoldOut();
            }
            if (target == ReleaseTarget.HIDDEN || target == ReleaseTarget.ALL) {
                product.activate();
            }
            productRepository.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult changeProductsSoldOutUntil(
        ShopId shopId,
        List<ProductId> productIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        validateSoldOutUntil(soldOutUntil, now);
        LoadedProducts loaded = loadProducts(shopId, productIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (!product.isSoldOut()) {
                failed.add(ProductAvailabilityFailure.of(
                    product.getId(), product.getName(), ErrorCode.PRODUCT_NOT_SOLD_OUT));
                continue;
            }
            product.changeSoldOutUntil(soldOutUntil);
            productRepository.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    public ProductAvailabilityChangeResult markOptionsSoldOut(
        ShopId shopId,
        List<ProductOptionId> optionIds,
        List<ProductCommonOptionId> commonOptionIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        validateSoldOutUntil(soldOutUntil, now);
        LoadedOptions loaded = loadOptions(shopId, optionIds, commonOptionIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        OptionBlockPlan plan = planBlockableOptions(loaded, failed, option -> !option.isSoldOut(),
            common -> !common.isSoldOut());

        List<Long> succeeded = new ArrayList<>();
        for (ProductOption option : plan.options()) {
            if (soldOutUntil != null) {
                option.markSoldOut(soldOutUntil);
            } else {
                option.markSoldOut();
            }
            productOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        for (ProductCommonOption option : plan.commonOptions()) {
            if (soldOutUntil != null) {
                option.markSoldOut(soldOutUntil);
            } else {
                option.markSoldOut();
            }
            productCommonOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        succeeded.addAll(plan.alreadyInTargetState());

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    public ProductAvailabilityChangeResult hideOptions(
        ShopId shopId,
        List<ProductOptionId> optionIds,
        List<ProductCommonOptionId> commonOptionIds
    ) {
        LoadedOptions loaded = loadOptions(shopId, optionIds, commonOptionIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        OptionBlockPlan plan = planBlockableOptions(loaded, failed, ProductOption::isVisible,
            ProductCommonOption::isVisible);

        List<Long> succeeded = new ArrayList<>();
        for (ProductOption option : plan.options()) {
            option.hide();
            productOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        for (ProductCommonOption option : plan.commonOptions()) {
            option.hide();
            productCommonOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        succeeded.addAll(plan.alreadyInTargetState());

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    public ProductAvailabilityChangeResult releaseOptions(
        ShopId shopId,
        List<ProductOptionId> optionIds,
        List<ProductCommonOptionId> commonOptionIds,
        ReleaseTarget target
    ) {
        LoadedOptions loaded = loadOptions(shopId, optionIds, commonOptionIds);

        List<Long> succeeded = new ArrayList<>();
        for (ProductOption option : loaded.options()) {
            if (target == ReleaseTarget.SOLD_OUT || target == ReleaseTarget.ALL) {
                option.releaseSoldOut();
            }
            if (target == ReleaseTarget.HIDDEN || target == ReleaseTarget.ALL) {
                option.activate();
            }
            productOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        for (ProductCommonOption option : loaded.commonOptions()) {
            if (target == ReleaseTarget.SOLD_OUT || target == ReleaseTarget.ALL) {
                option.releaseSoldOut();
            }
            if (target == ReleaseTarget.HIDDEN || target == ReleaseTarget.ALL) {
                option.activate();
            }
            productCommonOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult changeOptionsSoldOutUntil(
        ShopId shopId,
        List<ProductOptionId> optionIds,
        List<ProductCommonOptionId> commonOptionIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        validateSoldOutUntil(soldOutUntil, now);
        LoadedOptions loaded = loadOptions(shopId, optionIds, commonOptionIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        List<Long> succeeded = new ArrayList<>();
        for (ProductOption option : loaded.options()) {
            if (!option.isSoldOut()) {
                failed.add(ProductAvailabilityFailure.of(
                    option.getId(), option.getName(), ErrorCode.PRODUCT_NOT_SOLD_OUT));
                continue;
            }
            option.changeSoldOutUntil(soldOutUntil);
            productOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        for (ProductCommonOption option : loaded.commonOptions()) {
            if (!option.isSoldOut()) {
                failed.add(ProductAvailabilityFailure.of(
                    option.getId(), option.getName(), ErrorCode.PRODUCT_NOT_SOLD_OUT));
                continue;
            }
            option.changeSoldOutUntil(soldOutUntil);
            productCommonOptionRepository.save(option);
            succeeded.add(option.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    private LoadedProducts loadProducts(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, distinctIds);

        Map<Long, Product> byId = new LinkedHashMap<>();
        found.forEach(product -> byId.put(product.getId(), product));

        List<ProductAvailabilityFailure> failed = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            if (!byId.containsKey(productId.value())) {
                failed.add(ProductAvailabilityFailure.of(productId.value(), null, ErrorCode.PRODUCT_NOT_FOUND));
            }
        }
        return new LoadedProducts(List.copyOf(byId.values()), failed);
    }

    private LoadedOptions loadOptions(
        ShopId shopId,
        List<ProductOptionId> optionIds,
        List<ProductCommonOptionId> commonOptionIds
    ) {
        List<ProductOptionId> distinctOptionIds = distinct(optionIds);
        List<ProductCommonOptionId> distinctCommonIds = distinct(commonOptionIds);

        List<ProductOption> options = distinctOptionIds.isEmpty()
            ? List.of()
            : productOptionRepository.findAllByIdIn(distinctOptionIds);
        List<ProductCommonOption> commonOptions = distinctCommonIds.isEmpty()
            ? List.of()
            : productCommonOptionRepository.findAllByIdIn(distinctCommonIds);

        Map<Long, ProductOptionGroup> optionGroups = loadOptionGroups(options);
        Map<Long, ProductCommonOptionGroup> commonGroups = loadCommonOptionGroups(commonOptions);

        Map<Long, ShopId> optionGroupShopIds = loadOptionGroupShopIds(optionGroups);
        Map<Long, ShopId> commonOptionGroupShopIds = loadCommonOptionGroupShopIds(commonGroups);

        List<ProductAvailabilityFailure> failed = new ArrayList<>();

        List<ProductOption> ownedOptions = new ArrayList<>();
        Map<Long, ProductOption> optionById = new LinkedHashMap<>();
        options.forEach(option -> optionById.put(option.getId(), option));
        for (ProductOptionId optionId : distinctOptionIds) {
            ProductOption option = optionById.get(optionId.value());
            if (option == null) {
                failed.add(ProductAvailabilityFailure.of(optionId.value(), null, ErrorCode.PRODUCT_NOT_FOUND));
                continue;
            }
            ProductOptionGroup group = optionGroups.get(option.getOptionGroupId().value());
            if (group == null || notOwnedBy(shopId, optionGroupShopIds, option.getOptionGroupId().value())) {
                failed.add(ProductAvailabilityFailure.of(
                    option.getId(), option.getName(), ErrorCode.PRODUCT_NOT_FOUND));
                continue;
            }
            ownedOptions.add(option);
        }

        List<ProductCommonOption> ownedCommonOptions = new ArrayList<>();
        Map<Long, ProductCommonOption> commonById = new LinkedHashMap<>();
        commonOptions.forEach(option -> commonById.put(option.getId(), option));
        for (ProductCommonOptionId commonOptionId : distinctCommonIds) {
            ProductCommonOption option = commonById.get(commonOptionId.value());
            if (option == null) {
                failed.add(ProductAvailabilityFailure.of(
                    commonOptionId.value(), null, ErrorCode.PRODUCT_NOT_FOUND));
                continue;
            }
            ProductCommonOptionGroup group = commonGroups.get(option.getOptionGroupId().value());
            if (group == null
                || notOwnedBy(shopId, commonOptionGroupShopIds, option.getOptionGroupId().value())) {
                failed.add(ProductAvailabilityFailure.of(
                    option.getId(), option.getName(), ErrorCode.PRODUCT_NOT_FOUND));
                continue;
            }
            ownedCommonOptions.add(option);
        }

        return new LoadedOptions(ownedOptions, ownedCommonOptions, optionGroups, commonGroups, failed);
    }

    private OptionBlockPlan planBlockableOptions(
        LoadedOptions loaded,
        List<ProductAvailabilityFailure> failed,
        java.util.function.Predicate<ProductOption> blockable,
        java.util.function.Predicate<ProductCommonOption> commonBlockable
    ) {
        List<Long> alreadyInTargetState = new ArrayList<>();

        List<ProductOption> targets = new ArrayList<>();
        for (ProductOption option : loaded.options()) {
            if (blockable.test(option)) {
                targets.add(option);
            } else {
                alreadyInTargetState.add(option.getId());
            }
        }
        List<ProductCommonOption> commonTargets = new ArrayList<>();
        for (ProductCommonOption option : loaded.commonOptions()) {
            if (commonBlockable.test(option)) {
                commonTargets.add(option);
            } else {
                alreadyInTargetState.add(option.getId());
            }
        }

        List<ProductOption> allowed = new ArrayList<>();
        List<ProductCommonOption> allowedCommon = new ArrayList<>();

        Map<Long, List<ProductOption>> byGroup = groupBy(targets, option -> option.getOptionGroupId().value());
        for (Map.Entry<Long, List<ProductOption>> entry : byGroup.entrySet()) {
            ProductOptionGroup group = loaded.optionGroups().get(entry.getKey());
            int minRemaining = minRemaining(
                group == null ? null : group.getMinSelect(),
                group == null ? null : group.getMaxSelect()
            );
            List<ProductOption> groupTargets = sortedBySort(entry.getValue(), ProductOption::getSort);

            long selectable = productOptionRepository
                .findAllByOptionGroupId(ProductOptionGroupId.of(entry.getKey())).stream()
                .filter(option -> !option.isSoldOut() && option.isVisible())
                .count();

            int shortfall = (int) Math.max(0, minRemaining - (selectable - groupTargets.size()));
            for (int i = 0; i < groupTargets.size(); i++) {
                if (i >= groupTargets.size() - shortfall) {
                    ProductOption rejected = groupTargets.get(i);
                    failed.add(ProductAvailabilityFailure.of(rejected.getId(), rejected.getName(),
                        blockViolationCode(group == null ? null : group.getMinSelect(), minRemaining)));
                } else {
                    allowed.add(groupTargets.get(i));
                }
            }
        }

        Map<Long, List<ProductCommonOption>> commonByGroup =
            groupBy(commonTargets, option -> option.getOptionGroupId().value());
        for (Map.Entry<Long, List<ProductCommonOption>> entry : commonByGroup.entrySet()) {
            ProductCommonOptionGroup group = loaded.commonOptionGroups().get(entry.getKey());
            int minRemaining = minRemaining(
                group == null ? null : group.getMinSelect(),
                group == null ? null : group.getMaxSelect()
            );
            List<ProductCommonOption> groupTargets =
                sortedBySort(entry.getValue(), ProductCommonOption::getSort);

            long selectable = productCommonOptionRepository
                .findAllByOptionGroupId(ProductOptionGroupId.of(entry.getKey())).stream()
                .filter(option -> !option.isSoldOut() && option.isVisible())
                .count();

            int shortfall = (int) Math.max(0, minRemaining - (selectable - groupTargets.size()));
            for (int i = 0; i < groupTargets.size(); i++) {
                if (i >= groupTargets.size() - shortfall) {
                    ProductCommonOption rejected = groupTargets.get(i);
                    failed.add(ProductAvailabilityFailure.of(rejected.getId(), rejected.getName(),
                        blockViolationCode(group == null ? null : group.getMinSelect(), minRemaining)));
                } else {
                    allowedCommon.add(groupTargets.get(i));
                }
            }
        }

        return new OptionBlockPlan(allowed, allowedCommon, alreadyInTargetState);
    }

    private void rejectFromTail(
        List<Product> candidates,
        Map<Long, ProductAvailabilityFailure> rejected,
        long shortfall,
        ErrorCode errorCode,
        java.util.function.Predicate<Product> predicate
    ) {
        long remaining = shortfall;
        for (int i = candidates.size() - 1; i >= 0 && remaining > 0; i--) {
            Product product = candidates.get(i);
            if (!predicate.test(product) || rejected.containsKey(product.getId())) {
                continue;
            }
            rejected.put(product.getId(),
                ProductAvailabilityFailure.of(product.getId(), product.getName(), errorCode));
            remaining--;
        }
    }

    private Map<Long, ProductOptionGroup> loadOptionGroups(List<ProductOption> options) {
        List<ProductOptionGroupId> groupIds = options.stream()
            .map(ProductOption::getOptionGroupId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProductOptionGroup> byId = new LinkedHashMap<>();
        productOptionGroupRepository.findAllByIdIn(groupIds)
            .forEach(group -> byId.put(group.getId(), group));
        return byId;
    }

    private Map<Long, ProductCommonOptionGroup> loadCommonOptionGroups(List<ProductCommonOption> options) {
        List<ProductOptionGroupId> groupIds = options.stream()
            .map(ProductCommonOption::getOptionGroupId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProductCommonOptionGroup> byId = new LinkedHashMap<>();
        productCommonOptionGroupRepository.findAllByIdIn(groupIds)
            .forEach(group -> byId.put(group.getId(), group));
        return byId;
    }

    private Map<Long, ShopId> loadOptionGroupShopIds(Map<Long, ProductOptionGroup> optionGroups) {
        List<ProductOptionGroupId> groupIds = optionGroups.keySet().stream()
            .map(ProductOptionGroupId::of)
            .toList();
        if (groupIds.isEmpty()) {
            return Map.of();
        }

        List<ProductOptionGroupLink> links = productOptionGroupLinkRepository
            .findAllByOptionGroupIdIn(groupIds);
        Map<Long, ShopId> shopIdByProductId = loadShopIdsOf(links.stream()
            .map(ProductOptionGroupLink::getProductId)
            .toList());

        Map<Long, ShopId> byGroupId = new LinkedHashMap<>();
        for (ProductOptionGroupLink link : links) {
            byGroupId.computeIfAbsent(link.getOptionGroupId().value(),
                key -> shopIdByProductId.get(link.getProductId().value()));
        }
        return byGroupId;
    }

    private Map<Long, ShopId> loadCommonOptionGroupShopIds(Map<Long, ProductCommonOptionGroup> commonGroups) {
        List<ProductOptionGroupId> groupIds = commonGroups.keySet().stream()
            .map(ProductOptionGroupId::of)
            .toList();
        if (groupIds.isEmpty()) {
            return Map.of();
        }

        List<ProductCommonOptionGroupLink> links = productCommonOptionGroupLinkRepository
            .findAllByOptionGroupIdIn(groupIds);
        Map<Long, ShopId> shopIdByProductId = loadShopIdsOf(links.stream()
            .map(ProductCommonOptionGroupLink::getProductId)
            .toList());

        Map<Long, ShopId> byGroupId = new LinkedHashMap<>();
        for (ProductCommonOptionGroupLink link : links) {
            byGroupId.computeIfAbsent(link.getOptionGroupId().value(),
                key -> shopIdByProductId.get(link.getProductId().value()));
        }
        return byGroupId;
    }

    private Map<Long, ShopId> loadShopIdsOf(List<ProductId> productIds) {
        Map<Long, ShopId> shopIdByProductId = new LinkedHashMap<>();
        for (ProductId productId : distinct(productIds)) {
            productRepository.findById(productId)
                .ifPresent(product -> shopIdByProductId.put(product.getId(), product.getShopId()));
        }
        return shopIdByProductId;
    }

    private boolean notOwnedBy(ShopId shopId, Map<Long, ShopId> shopIdByOptionGroupId, Long optionGroupId) {
        ShopId owner = shopIdByOptionGroupId.get(optionGroupId);
        return owner == null || !owner.equals(shopId);
    }

    private int minRemaining(Integer minSelect, Integer maxSelect) {
        return ProductOptionSelectionRule.minRemaining(minSelect, maxSelect);
    }

    private ErrorCode blockViolationCode(Integer minSelect, int minRemaining) {
        int minBound = Math.max(minSelect != null ? minSelect : 0, 1);
        return minRemaining > minBound
            ? ErrorCode.PRODUCT_OPTION_MAX_SELECT_VIOLATION
            : ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION;
    }

    private <T> List<T> distinct(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private <T> Map<Long, List<T>> groupBy(List<T> values, java.util.function.Function<T, Long> keyOf) {
        Map<Long, List<T>> byKey = new LinkedHashMap<>();
        values.forEach(value -> byKey.computeIfAbsent(keyOf.apply(value), key -> new ArrayList<>()).add(value));
        return byKey;
    }

    private <T> List<T> sortedBySort(List<T> values, java.util.function.Function<T, Integer> sortOf) {
        return values.stream()
            .sorted(Comparator.comparing(sortOf, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    private record LoadedProducts(
        List<Product> found,
        List<ProductAvailabilityFailure> failed
    ) {
    }

    private record LoadedOptions(
        List<ProductOption> options,
        List<ProductCommonOption> commonOptions,
        Map<Long, ProductOptionGroup> optionGroups,
        Map<Long, ProductCommonOptionGroup> commonOptionGroups,
        List<ProductAvailabilityFailure> failed
    ) {
    }

    private record OptionBlockPlan(
        List<ProductOption> options,
        List<ProductCommonOption> commonOptions,
        List<Long> alreadyInTargetState
    ) {
    }
}
