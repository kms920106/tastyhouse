package com.tastyhouse.ceoapi.product.application.service;

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

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergePreviewGroupResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergePreviewOptionResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergePreviewResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergeSuggestionGroupResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergeSuggestionOptionResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupMergeSuggestionResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionGroupMergeQueryUseCase;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.service.ProductOptionGroupSignature;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

/**
 * 옵션그룹 합치기의 조회 측(추천 목록 · 미리보기 diff).
 *
 * <p>write 포트를 주입하지 않는다(CQRS 교차 주입 금지) — 제외 목록도 DAO를 통해 읽는다.
 * 소유권 검증은 write 포트를 내부에 감싼 협력 빈 {@link ShopOwnershipValidator}를 경유한다.
 *
 * <p><b>diff 계산은 여기서 한다</b> — 어느 항목을 "다르다"고 표시할지는 화면의 관심사이지 도메인
 * 불변식이 아니다. 합치기의 실제 동작(기준이 이긴다)은 도메인이 소유하고, 이 서비스는 그 결과를
 * 사람이 수락할 수 있게 보여줄 뿐이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductOptionGroupMergeQueryService implements ProductOptionGroupMergeQueryUseCase {

    /** 기준과 후보가 완전히 같은 옵션. */
    private static final String DIFF_SAME = "SAME";
    /** 후보에만 있는 옵션 — <b>합치면 사라진다</b>(재부모화하지 않으므로). */
    private static final String DIFF_ONLY_IN_CANDIDATE = "ONLY_IN_CANDIDATE";
    /** 이름은 같은데 가격이 다른 옵션 — 합치면 기준 가격이 이긴다. */
    private static final String DIFF_PRICE_DIFFERS = "PRICE_DIFFERS";

    private final ProductQueryPort productQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupMergeQueryService(
        ProductQueryPort productQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productQueryPort = productQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 합치기 추천 묶음 목록을 반환한다 — 동일성 서명이 같은 그룹이 2개 이상인 묶음만 담는다.
     *
     * <p>DAO는 원시 payload만 돌려주고 <b>SHA-256은 여기서 계산</b>한다
     * ({@link ProductOptionGroupSignature} 참조 — SQL과 Java 두 벌로 유지하면 인코딩 차이만으로
     * 제외 기능이 조용히 깨진다). 계산한 서명으로 점주가 [X]로 제외한 묶음을 걸러낸다.
     */
    @Override
    public List<ProductOptionGroupMergeSuggestionResponse> getMergeSuggestions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductOptionGroupMergeCandidateResult> candidates =
            productQueryPort.findOptionGroupMergeCandidates(shopId);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Set<String> excluded = productQueryPort.findOptionGroupMergeExcludedSignatures(shopId);
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productQueryPort.findLinkedProductsByShop(shopId);
        Map<Long, ProductOptionGroupManagementResult> groupById =
            productQueryPort.findProductOptionGroupsForManagement(shopId).stream()
                .collect(Collectors.toMap(ProductOptionGroupManagementResult::id, group -> group,
                    (first, second) -> first, LinkedHashMap::new));

        // payload가 같은 행들이 곧 하나의 묶음이다. DAO가 payload 순으로 정렬해 주므로 순서가 안정적이다.
        Map<String, List<ProductOptionGroupMergeCandidateResult>> byPayload = candidates.stream()
            .collect(Collectors.groupingBy(
                ProductOptionGroupMergeCandidateResult::signaturePayload,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<ProductOptionGroupMergeSuggestionResponse> suggestions = new ArrayList<>();
        for (Map.Entry<String, List<ProductOptionGroupMergeCandidateResult>> entry : byPayload.entrySet()) {
            String signature = ProductOptionGroupSignature.hash(entry.getKey());
            if (excluded.contains(signature)) {
                continue;
            }
            suggestions.add(toSuggestionResponse(signature, entry.getValue(), groupById, linkedByGroupId));
        }
        return suggestions;
    }

    /**
     * 직접 선택 경로의 미리보기 — 기준 그룹과 후보들의 차이를 계산해 돌려준다.
     *
     * <p>{@code mergeable}은 <b>사전</b> 판정이다. 실행 시점의 진짜 판정은 도메인 서비스가 다시 하므로,
     * 여기서 통과했다고 실행이 보장되지는 않는다(그 사이 다른 탭에서 상태가 바뀔 수 있다). 목적은
     * 되돌릴 수 없는 버튼을 누르기 전에 명백한 불가 사유를 먼저 보여주는 것이다.
     */
    @Override
    public ProductOptionGroupMergePreviewResponse getMergePreview(
        Long ceoId,
        Long shopId,
        Long baseOptionGroupId,
        List<Long> optionGroupIds
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Map<Long, ProductOptionGroupManagementResult> groupById =
            productQueryPort.findProductOptionGroupsForManagement(shopId).stream()
                .collect(Collectors.toMap(ProductOptionGroupManagementResult::id, group -> group,
                    (first, second) -> first, LinkedHashMap::new));

        // 이 가게의 관리 목록에 없는 id는 미존재·타 가게·고아를 구분하지 않고 404로 묶는다
        // (존재 여부 자체를 흘리지 않는 기존 정책).
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
            productQueryPort.findLinkedProductsByShop(shopId);

        String blockedReason = findBlockedReason(base, candidates, linkedByGroupId);
        return ProductOptionGroupMergePreviewResponse.from(
            toPreviewGroupResponse(base, base, linkedByGroupId, true),
            candidates.stream()
                .map(candidate -> toPreviewGroupResponse(candidate, base, linkedByGroupId, false))
                .toList(),
            blockedReason == null,
            blockedReason
        );
    }

    /**
     * 실행 전에 확정적으로 알 수 있는 불가 사유를 찾는다. 없으면 {@code null}.
     *
     * <p>도메인 서비스의 검증 순서와 <b>같은 사유 코드</b>를 쓴다 — 미리보기가 "가능"이라 했는데 실행이
     * 다른 이유로 거절되면 사용자는 무엇을 고쳐야 할지 알 수 없다.
     */
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
        // 보증금 그룹과 일반 그룹은 금액의 성격(과세/비과세)이 달라 섞을 수 없다.
        if (candidates.stream().anyMatch(candidate -> !Objects.equals(candidate.groupType(), base.groupType()))) {
            return ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH.getCode();
        }

        // 같은 메뉴 공유는 base-vs-각각이 아니라 집합 전체 pairwise로 본다 — 흡수 대상 둘이 같은 메뉴를
        // 공유해도 그 메뉴의 링크가 2개→1개로 조용히 줄어든다.
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

    private ProductOptionGroupMergeSuggestionResponse toSuggestionResponse(
        String signature,
        List<ProductOptionGroupMergeCandidateResult> members,
        Map<Long, ProductOptionGroupManagementResult> groupById,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId
    ) {
        ProductOptionGroupMergeCandidateResult representative = members.getFirst();

        // 묶음 안의 그룹은 정의상 옵션 집합이 같으므로 대표 1세트만 내려보낸다(화면도 하나만 그린다).
        // 숨은 옵션은 서명에 참여하지 않았으므로 표시에서도 제외해 화면과 판정 기준을 일치시킨다.
        List<ProductOptionGroupMergeSuggestionOptionResponse> options =
            optionsOf(groupById.get(representative.optionGroupId())).stream()
                .filter(ProductOptionManagementResult::visible)
                .map(option -> ProductOptionGroupMergeSuggestionOptionResponse.from(
                    option.id(),
                    option.name(),
                    option.additionalPrice()
                ))
                .toList();

        List<ProductOptionGroupMergeSuggestionGroupResponse> groups = members.stream()
            .map(member -> {
                List<String> linkedProductNames =
                    linkedProductNamesOf(member.optionGroupId(), linkedByGroupId);
                return ProductOptionGroupMergeSuggestionGroupResponse.from(
                    member.optionGroupId(),
                    linkedProductNames.size(),
                    linkedProductNames
                );
            })
            .toList();

        int linkedProductCount = groups.stream()
            .mapToInt(ProductOptionGroupMergeSuggestionGroupResponse::linkedProductCount)
            .sum();

        return ProductOptionGroupMergeSuggestionResponse.from(
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

    private ProductOptionGroupMergePreviewGroupResponse toPreviewGroupResponse(
        ProductOptionGroupManagementResult group,
        ProductOptionGroupManagementResult base,
        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId,
        boolean isBase
    ) {
        return ProductOptionGroupMergePreviewGroupResponse.from(
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
            toPreviewOptionResponses(group, base, isBase)
        );
    }

    /**
     * 옵션 diff를 계산한다. 판정 키는 <b>옵션명</b>이다 — 합치기 판단에서 사람이 "같은 옵션"으로 보는
     * 것은 이름이고, 가격 차이는 그 위에 얹히는 정보이기 때문이다.
     *
     * <p>기준 그룹에는 {@code ONLY_IN_BASE}가 <b>나타나지 않는다</b> — 기준의 옵션은 전부 남으므로
     * "기준에만 있음"이라는 경고가 의미가 없다. 그 표시는 후보 쪽에서 사라질 옵션
     * ({@code ONLY_IN_CANDIDATE})을 드러내는 데 쓴다.
     */
    private List<ProductOptionGroupMergePreviewOptionResponse> toPreviewOptionResponses(
        ProductOptionGroupManagementResult group,
        ProductOptionGroupManagementResult base,
        boolean isBase
    ) {
        Map<String, ProductOptionManagementResult> baseOptionByName = optionsOf(base).stream()
            .collect(Collectors.toMap(ProductOptionManagementResult::name, option -> option,
                (first, second) -> first, LinkedHashMap::new));

        return optionsOf(group).stream()
            .map(option -> ProductOptionGroupMergePreviewOptionResponse.from(
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
