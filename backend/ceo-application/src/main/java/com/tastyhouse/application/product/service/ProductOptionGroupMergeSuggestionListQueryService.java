package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOptionGroupSignature;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeSuggestionListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductOptionGroupMergeSuggestionListQueryService implements ProductOptionGroupMergeSuggestionListQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupMergeSuggestionListQueryService(
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
}
