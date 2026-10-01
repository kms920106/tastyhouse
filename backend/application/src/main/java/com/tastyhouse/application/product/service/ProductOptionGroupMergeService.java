package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.service.ProductOptionSelectionRule;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;

public class ProductOptionGroupMergeService {

    private final ProductOptionGroupPersistencePort optionGroupPersistencePort;
    private final ProductOptionPersistencePort optionPersistencePort;
    private final ProductOptionGroupLinkPersistencePort linkPersistencePort;
    private final ProductOptionGroupLinkService linkService;
    private final ProductOptionGroupMergeHistoryPersistencePort mergeHistoryPersistencePort;

    public ProductOptionGroupMergeService(
        ProductOptionGroupPersistencePort optionGroupPersistencePort,
        ProductOptionPersistencePort optionPersistencePort,
        ProductOptionGroupLinkPersistencePort linkPersistencePort,
        ProductOptionGroupLinkService linkService,
        ProductOptionGroupMergeHistoryPersistencePort mergeHistoryPersistencePort
    ) {
        this.optionGroupPersistencePort = optionGroupPersistencePort;
        this.optionPersistencePort = optionPersistencePort;
        this.linkPersistencePort = linkPersistencePort;
        this.linkService = linkService;
        this.mergeHistoryPersistencePort = mergeHistoryPersistencePort;
    }

    public Long merge(
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        List<ProductOptionGroupId> targetOptionGroupIds,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        List<Long> targetIds = distinctTargetIds(baseOptionGroupId, targetOptionGroupIds);

        ProductOptionGroup base = loadGroup(baseOptionGroupId.value());
        List<ProductOptionGroup> targets = targetIds.stream().map(this::loadGroup).toList();

        validateNotHidden(base);
        targets.forEach(this::validateNotHidden);

        Map<Long, List<ProductOptionGroupLink>> linksByGroupId = loadLinks(base, targets);
        validateSingleShop(shopId, linksByGroupId);
        validateNoSharedProduct(base, targets, linksByGroupId);
        validateSameGroupType(base, targets);
        validateBaseSelectable(base);

        return applyMerge(shopId, base, targets, linksByGroupId, entryType, actorCeoId);
    }

    private List<Long> distinctTargetIds(
        ProductOptionGroupId baseOptionGroupId,
        List<ProductOptionGroupId> targetOptionGroupIds
    ) {
        if (targetOptionGroupIds == null || targetOptionGroupIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
        }

        Set<Long> unique = new LinkedHashSet<>();
        targetOptionGroupIds.stream()
            .filter(Objects::nonNull)
            .forEach(id -> unique.add(id.value()));

        if (unique.contains(baseOptionGroupId.value())) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_BASE_INCLUDED);
        }
        if (unique.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
        }
        return List.copyOf(unique);
    }

    private ProductOptionGroup loadGroup(Long optionGroupId) {
        return optionGroupPersistencePort.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
    }

    private void validateNotHidden(ProductOptionGroup group) {
        if (!group.isVisible()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET);
        }
    }

    private Map<Long, List<ProductOptionGroupLink>> loadLinks(
        ProductOptionGroup base,
        List<ProductOptionGroup> targets
    ) {
        List<ProductOptionGroupId> groupIds = new ArrayList<>();
        groupIds.add(base.getProductOptionGroupId());
        targets.forEach(group -> groupIds.add(group.getProductOptionGroupId()));

        Map<Long, List<ProductOptionGroupLink>> byGroupId = new LinkedHashMap<>();
        for (ProductOptionGroupLink link : linkPersistencePort.findAllByOptionGroupIdIn(groupIds)) {
            byGroupId.computeIfAbsent(link.getOptionGroupId().value(), key -> new ArrayList<>()).add(link);
        }

        for (ProductOptionGroupId groupId : groupIds) {
            if (byGroupId.getOrDefault(groupId.value(), List.of()).isEmpty()) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
            }
        }
        return byGroupId;
    }

    private void validateSingleShop(ShopId shopId, Map<Long, List<ProductOptionGroupLink>> linksByGroupId) {
        for (Long groupId : linksByGroupId.keySet()) {
            ShopId owner = linkService.findOwningShopId(ProductOptionGroupId.of(groupId));
            if (owner == null) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
            }
            if (!owner.equals(shopId)) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
            }
        }
    }

    private void validateNoSharedProduct(
        ProductOptionGroup base,
        List<ProductOptionGroup> targets,
        Map<Long, List<ProductOptionGroupLink>> linksByGroupId
    ) {
        List<ProductOptionGroup> all = new ArrayList<>();
        all.add(base);
        all.addAll(targets);

        Map<Long, Long> ownerGroupIdByProductId = new LinkedHashMap<>();
        for (ProductOptionGroup group : all) {
            Long groupId = group.getId();
            for (ProductOptionGroupLink link : linksByGroupId.getOrDefault(groupId, List.of())) {
                Long productId = link.getProductId().value();
                Long previous = ownerGroupIdByProductId.putIfAbsent(productId, groupId);
                if (previous != null && !previous.equals(groupId)) {
                    throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED);
                }
            }
        }
    }

    private void validateSameGroupType(ProductOptionGroup base, List<ProductOptionGroup> targets) {
        for (ProductOptionGroup target : targets) {
            if (target.getGroupType() != base.getGroupType()) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH);
            }
        }
    }

    private void validateBaseSelectable(ProductOptionGroup base) {
        List<ProductOption> baseOptions =
            optionPersistencePort.findAllByOptionGroupId(base.getProductOptionGroupId());
        long selectable = baseOptions.stream().filter(ProductOptionSelectionRule::selectable).count();
        if (selectable < ProductOptionSelectionRule.minRemaining(base)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
        }
    }

    private Long applyMerge(
        ShopId shopId,
        ProductOptionGroup base,
        List<ProductOptionGroup> targets,
        Map<Long, List<ProductOptionGroupLink>> linksByGroupId,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        for (ProductOptionGroup target : targets) {
            List<ProductId> productIds = linksByGroupId.getOrDefault(target.getId(), List.of()).stream()
                .map(ProductOptionGroupLink::getProductId)
                .toList();
            linkService.relink(productIds, target.getProductOptionGroupId(), base.getProductOptionGroupId());

            hideOptionsOf(target);
            target.hide();
            optionGroupPersistencePort.save(target);

            mergeHistoryPersistencePort.save(ProductOptionGroupMergeHistory.of(
                shopId,
                base.getProductOptionGroupId(),
                target.getProductOptionGroupId(),
                target.getName(),
                entryType,
                actorCeoId
            ));
        }
        return base.getId();
    }

    private void hideOptionsOf(ProductOptionGroup target) {
        for (ProductOption option : optionPersistencePort.findAllByOptionGroupId(target.getProductOptionGroupId())) {
            if (!option.isVisible()) {
                continue;
            }
            option.hide();
            optionPersistencePort.save(option);
        }
    }
}
