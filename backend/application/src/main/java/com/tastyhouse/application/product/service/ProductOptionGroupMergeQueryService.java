package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.service.ProductOptionGroupSignature;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergePreviewResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductOptionGroupMergeQueryService implements ProductOptionGroupMergeQueryUseCase {

    private static final String DIFF_SAME = "SAME";

    private static final String DIFF_ONLY_IN_CANDIDATE = "ONLY_IN_CANDIDATE";

    private static final String DIFF_PRICE_DIFFERS = "PRICE_DIFFERS";

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupMergeQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOptionGroupMergeSuggestionResult> getMergeSuggestions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductOptionGroupMergeCandidateResult> candidates =
            productOwnerQueryPort.findOptionGroupMergeCandidates(shopId);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Set<String> excluded = productOwnerQueryPort.findOptionGroupMergeExcludedSignatures(shopId);
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productOwnerQueryPort.findLinkedProductsByShop(shopId);
        Map<Long, ProductOptionGroupManagementResult> groupById =
            productOwnerQueryPort.findProductOptionGroupsForManagement(shopId).stream()
                .collect(Collectors.toMap(ProductOptionGroupManagementResult::id, group -> group,
                    (first, second) -> first, LinkedHashMap::new));

        Map<String, List<ProductOptionGroupMergeCandidateResult>> byPayload = candidates.stream()
            .collect(Collectors.groupingBy(
                ProductOptionGroupMergeCandidateResult::signaturePayload,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<ProductOptionGroupMergeSuggestionResult> suggestions = new ArrayList<>();
        for (Map.Entry<String, List<ProductOptionGroupMergeCandidateResult>> entry : byPayload.entrySet()) {
            String signature = ProductOptionGroupSignature.hash(entry.getKey());
            if (excluded.contains(signature)) {
                continue;
            }
            suggestions.add(toSuggestionResult(signature, entry.getValue(), groupById, linkedByGroupId));
        }
        return suggestions;
    }

    @Override
    public ProductOptionGroupMergePreviewResult getMergePreview(
        Long ceoId,
        Long shopId,
        Long baseOptionGroupId,
        List<Long> optionGroupIds
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Map<Long, ProductOptionGroupManagementResult> groupById =
            productOwnerQueryPort.findProductOptionGroupsForManagement(shopId).stream()
                .collect(Collectors.toMap(ProductOptionGroupManagementResult::id, group -> group,
                    (first, second) -> first, LinkedHashMap::new));

        ProductOptionGroupManagementResult base = groupById.get(baseOptionGroupId);
        if (base == null) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }

        List<ProductOptionGroupManagementResult> candidates = new ArrayList<>();
        for (Long optionGroupId : distinct(optionGroupIds)) {
            if (Objects.equals(optionGroupId, baseOptionGroupId)) {
                continue;
            }
            ProductOptionGroupManagementResult candidate = groupById.get(optionGroupId);
            if (candidate == null) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
            }
            candidates.add(candidate);
        }

        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productOwnerQueryPort.findLinkedProductsByShop(shopId);

        String blockedReason = findBlockedReason(base, candidates, linkedByGroupId);
        return new ProductOptionGroupMergePreviewResult(
            toPreviewGroupResult(base, base, linkedByGroupId, true),
            candidates.stream()
                .map(candidate -> toPreviewGroupResult(candidate, base, linkedByGroupId, false))
                .toList(),
            blockedReason == null,
            blockedReason
        );
    }

    private String findBlockedReason(
        ProductOptionGroupManagementResult base,
        List<ProductOptionGroupManagementResult> candidates,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId
    ) {
        if (candidates.isEmpty()) {
            return ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY.getCode();
        }
        if (!base.visible() || candidates.stream().anyMatch(candidate -> !candidate.visible())) {
            return ErrorCode.PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET.getCode();
        }

        if (candidates.stream().anyMatch(candidate -> !Objects.equals(candidate.groupType(), base.groupType()))) {
            return ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH.getCode();
        }

        List<ProductOptionGroupManagementResult> all = new ArrayList<>();
        all.add(base);
        all.addAll(candidates);

        Map<Long, Long> ownerGroupIdByProductId = new LinkedHashMap<>();
        for (ProductOptionGroupManagementResult group : all) {
            for (ProductOptionGroupLinkedProductResult linked
                : linkedByGroupId.getOrDefault(group.id(), List.of())) {
                Long previous = ownerGroupIdByProductId.putIfAbsent(linked.id(), group.id());
                if (previous != null && !previous.equals(group.id())) {
                    return ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED.getCode();
                }
            }
        }
        return null;
    }

    private ProductOptionGroupMergeSuggestionResult toSuggestionResult(
        String signature,
        List<ProductOptionGroupMergeCandidateResult> members,
        Map<Long, ProductOptionGroupManagementResult> groupById,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId
    ) {
        ProductOptionGroupMergeCandidateResult representative = members.getFirst();

        List<ProductOptionGroupMergeSuggestionResult.Option> options =
            optionsOf(groupById.get(representative.optionGroupId())).stream()
                .filter(ProductOptionManagementResult::visible)
                .map(option -> new ProductOptionGroupMergeSuggestionResult.Option(
                    option.id(),
                    option.name(),
                    option.additionalPrice()
                ))
                .toList();

        List<ProductOptionGroupMergeSuggestionResult.Group> groups = members.stream()
            .map(member -> {
                List<String> linkedProductNames =
                    linkedProductNamesOf(member.optionGroupId(), linkedByGroupId);
                return new ProductOptionGroupMergeSuggestionResult.Group(
                    member.optionGroupId(),
                    linkedProductNames.size(),
                    linkedProductNames
                );
            })
            .toList();

        int linkedProductCount = groups.stream()
            .mapToInt(ProductOptionGroupMergeSuggestionResult.Group::linkedProductCount)
            .sum();

        return new ProductOptionGroupMergeSuggestionResult(
            signature,
            representative.name(),
            representative.minSelect(),
            representative.maxSelect(),
            members.size(),
            linkedProductCount,
            options,
            groups
        );
    }

    private ProductOptionGroupMergePreviewResult.Group toPreviewGroupResult(
        ProductOptionGroupManagementResult group,
        ProductOptionGroupManagementResult base,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId,
        boolean isBase
    ) {
        return new ProductOptionGroupMergePreviewResult.Group(
            group.id(),
            group.name(),
            group.description(),
            group.required(),
            group.multipleSelect(),
            group.minSelect(),
            group.maxSelect(),
            linkedProductNamesOf(group.id(), linkedByGroupId),
            !isBase && !Objects.equals(group.name(), base.name()),
            !isBase && !Objects.equals(group.minSelect(), base.minSelect()),
            !isBase && !Objects.equals(group.maxSelect(), base.maxSelect()),
            toPreviewOptionResults(group, base, isBase)
        );
    }

    private List<ProductOptionGroupMergePreviewResult.Option> toPreviewOptionResults(
        ProductOptionGroupManagementResult group,
        ProductOptionGroupManagementResult base,
        boolean isBase
    ) {
        Map<String, ProductOptionManagementResult> baseOptionByName = optionsOf(base).stream()
            .collect(Collectors.toMap(ProductOptionManagementResult::name, option -> option,
                (first, second) -> first, LinkedHashMap::new));

        return optionsOf(group).stream()
            .map(option -> new ProductOptionGroupMergePreviewResult.Option(
                option.id(),
                option.name(),
                option.additionalPrice(),
                option.soldOut(),
                option.visible(),
                isBase ? DIFF_SAME : diffTypeOf(option, baseOptionByName.get(option.name()))
            ))
            .toList();
    }

    private String diffTypeOf(ProductOptionManagementResult option, ProductOptionManagementResult baseOption) {
        if (baseOption == null) {
            return DIFF_ONLY_IN_CANDIDATE;
        }
        if (!Objects.equals(option.additionalPrice(), baseOption.additionalPrice())) {
            return DIFF_PRICE_DIFFERS;
        }
        return DIFF_SAME;
    }

    private List<ProductOptionManagementResult> optionsOf(ProductOptionGroupManagementResult group) {
        return group == null || group.options() == null ? List.of() : group.options();
    }

    private List<String> linkedProductNamesOf(
        Long optionGroupId,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId
    ) {
        return linkedByGroupId.getOrDefault(optionGroupId, List.of()).stream()
            .map(ProductOptionGroupLinkedProductResult::name)
            .toList();
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(unique::add);
        return List.copyOf(unique);
    }
}
