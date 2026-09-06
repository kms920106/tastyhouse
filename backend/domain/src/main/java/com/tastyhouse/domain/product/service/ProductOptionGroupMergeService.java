package com.tastyhouse.domain.product.service;

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
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기(merge)의 단일 소유자 — 다중 애그리거트 불변식이라 도메인이 제자리다.
 *
 * <h2>합치기의 정의</h2>
 * <ul>
 *   <li><b>기준 그룹은 손대지 않는다.</b> "기준 옵션그룹"이 곧 살아남는 정의다. 기준을 덮어쓰면
 *       멱등성이 깨지고, 무엇보다 과거 주문에 박제된 옵션을 조용히 바꾸게 된다.</li>
 *   <li><b>흡수 그룹은 행을 남긴 채 감춘다</b>({@code hide()}) — {@code ORDER_PRODUCT_OPTION}이
 *       {@code option_group_id}로 이 행을 참조하므로 하드 삭제는 주문 이력을 끊는다.</li>
 *   <li><b>흡수 그룹의 옵션을 기준 그룹으로 재부모화하지 않는다</b> — 아래 항 참조.</li>
 *   <li><b>링크만 기준 그룹으로 옮긴다</b>(sort 보존 + 재정규화).</li>
 * </ul>
 *
 * <h2>옵션을 union(재부모화)하지 않는 이유</h2>
 * <ol>
 *   <li>합치기 확인 화면은 <b>기준 그룹의 옵션 목록 하나만</b> 보여준다. union이면 중복된 합집합이
 *       나와 화면이 약속한 것과 결과가 달라진다.</li>
 *   <li>추천 합치기는 옵션명·가격이 전부 같은 그룹만 제안하므로 union은 순수 중복 생성이다.</li>
 *   <li>{@code ORDER_PRODUCT_OPTION.option_id}가 옵션 행을 박제한다 — 재부모화는 과거 주문이
 *       참조하는 행의 소속 그룹을 <b>소급 변경</b>하는 것이다.</li>
 *   <li>기준 그룹의 min/maxSelect는 그대로인데 옵션 수가 N배가 되어 <b>검증한 적 없는 제약 상태</b>가
 *       된다.</li>
 *   <li>직접 선택 경로에서 "같은 이름 다른 가격 중 뭐가 이기나"라는 병합 충돌 정책을 정의할 필요가
 *       사라진다 — <b>기준이 이긴다</b>는 구조적 답이 나온다.</li>
 * </ol>
 * 즉 <b>파괴하지 않되 섞지도 않는다.</b> 이것이 "분리 불가"를 정직하게 만든다 — 되돌릴 대상이
 * 애초에 없기 때문이다. (분리 엔드포인트를 두지 않는 것이 이 성질의 표현이다.)
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가 담당한다.
 */
public class ProductOptionGroupMergeService {

    private final ProductOptionGroupRepository optionGroupRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionGroupLinkRepository linkRepository;
    private final ProductOptionGroupLinkService linkService;
    private final ProductOptionGroupMergeHistoryRepository mergeHistoryRepository;

    public ProductOptionGroupMergeService(
        ProductOptionGroupRepository optionGroupRepository,
        ProductOptionRepository optionRepository,
        ProductOptionGroupLinkRepository linkRepository,
        ProductOptionGroupLinkService linkService,
        ProductOptionGroupMergeHistoryRepository mergeHistoryRepository
    ) {
        this.optionGroupRepository = optionGroupRepository;
        this.optionRepository = optionRepository;
        this.linkRepository = linkRepository;
        this.linkService = linkService;
        this.mergeHistoryRepository = mergeHistoryRepository;
    }

    /**
     * 흡수 대상들을 기준 그룹으로 합친다. 살아남은 기준 그룹 id를 반환한다.
     *
     * <p>검증을 <b>전부 통과한 뒤에야</b> 변경을 시작한다 — 부분 적용된 합치기는 되돌릴 수 없다.
     */
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

    /**
     * 대상 목록을 정규화한다 — 중복 id 제거 후 재검사한다.
     *
     * <p>중복 제거를 검증보다 <b>먼저</b> 하는 이유: 같은 id를 두 번 실으면 흡수 처리가 두 번 돌아
     * 이력이 2행 쌓이고, 두 번째 처리는 이미 감춰진 그룹을 대상으로 삼아 엉뚱한 에러를 낸다.
     */
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
        return optionGroupRepository.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
    }

    /**
     * 이미 감춰진 그룹은 합치기 대상이 아니다.
     *
     * <p>이 저장소는 옵션그룹의 소프트 삭제를 {@code is_visible=0}으로 겸하므로, 감춰진 그룹은
     * "점주가 삭제한 그룹"이거나 "이미 다른 합치기에 흡수된 그룹"이다. 어느 쪽이든 다시 합칠 대상이
     * 아니다.
     */
    private void validateNotHidden(ProductOptionGroup group) {
        if (!group.isVisible()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET);
        }
    }

    /** 그룹별 링크를 <b>일괄</b> 로드한다(N+1 회피). 링크 0건인 고아 그룹은 여기서 걸러진다. */
    private Map<Long, List<ProductOptionGroupLink>> loadLinks(
        ProductOptionGroup base,
        List<ProductOptionGroup> targets
    ) {
        List<ProductOptionGroupId> groupIds = new ArrayList<>();
        groupIds.add(base.getProductOptionGroupId());
        targets.forEach(group -> groupIds.add(group.getProductOptionGroupId()));

        Map<Long, List<ProductOptionGroupLink>> byGroupId = new LinkedHashMap<>();
        for (ProductOptionGroupLink link : linkRepository.findAllByOptionGroupIdIn(groupIds)) {
            byGroupId.computeIfAbsent(link.getOptionGroupId().value(), key -> new ArrayList<>()).add(link);
        }

        for (ProductOptionGroupId groupId : groupIds) {
            // 연결 0건 = 고아 그룹. 소유 가게를 판정할 수 없으므로 "없음"과 같이 다룬다
            // (없음/타 가게/고아를 한 코드로 뭉쳐 IDOR 정보 노출을 막는 기존 정책).
            if (byGroupId.getOrDefault(groupId.value(), List.of()).isEmpty()) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
            }
        }
        return byGroupId;
    }

    /**
     * 모든 대상 그룹이 <b>요청한 그 가게</b>의 것인지 검증한다.
     *
     * <p>옵션그룹은 자기 가게를 모르므로 "그룹 → 링크 → 메뉴 → 가게" 역조회로 판정한다
     * ({@link ProductOptionGroupLinkService#findOwningShopId}의 단일 가게 불변식과 같은 근거).
     */
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

    /**
     * 같은 메뉴에 연결된 옵션그룹끼리는 합칠 수 없다(PDF 규칙).
     *
     * <p>이 규칙이 막는 것은 <b>링크 소실</b>이다. 두 그룹이 같은 메뉴에 걸려 있으면 그 메뉴의 링크가
     * 2개에서 1개로 조용히 줄어들어, 손님이 고르던 선택지 한 벌이 사라진다.
     * {@code UNIQUE (product_id, option_group_id)}가 물리적으로 그 상태를 만들 수 없게 하는데,
     * 이 검증이 그 충돌을 <b>변경 시작 전에</b> 사람이 읽을 수 있는 사유로 바꾼다.
     *
     * <p><b>base-vs-각각이 아니라 집합 전체 pairwise로 본다</b> — 흡수 대상 둘이 같은 메뉴를 공유해도
     * 같은 소실이 일어난다.
     */
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

    /**
     * 보증금 옵션그룹과 일반 옵션그룹은 합칠 수 없다.
     *
     * <p>두 유형은 <b>금액의 성격 자체가 다르다</b> — 일반 옵션의 추가금은 과세 매출이고 보증금은
     * 비과세·정산 제외 항목이다. 섞으면 흡수 그룹이 걸려 있던 메뉴들이 손님 화면에서 유형이 바뀐 그룹을
     * 보게 되고, 그 이후 주문의 금액 분류가 조용히 달라진다.
     *
     * <p>유형 전환 경로를 두지 않기로 한 결정({@code ProductOptionGroup.groupType}이 {@code final})의
     * 연장선이다 — 전환을 막아 놓고 합치기로 우회할 수 있으면 그 차단은 의미가 없다.
     */
    private void validateSameGroupType(ProductOptionGroup base, List<ProductOptionGroup> targets) {
        for (ProductOptionGroup target : targets) {
            if (target.getGroupType() != base.getGroupType()) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH);
            }
        }
    }

    /**
     * 기준 그룹이 자기 선택 제약을 만족하는지 확인한다.
     *
     * <p>합치기 후에는 흡수 그룹이 걸려 있던 메뉴들도 <b>기준 그룹만</b> 보게 되므로, 기준 그룹이
     * 하한을 채우지 못하는 상태면 그 메뉴들이 주문 불가가 된다. 기존 코드({@code MIN_SELECT_VIOLATION})를
     * 재사용해 사유를 알린다.
     */
    private void validateBaseSelectable(ProductOptionGroup base) {
        List<ProductOption> baseOptions =
            optionRepository.findAllByOptionGroupId(base.getProductOptionGroupId());
        long selectable = baseOptions.stream().filter(ProductOptionSelectionRule::selectable).count();
        if (selectable < ProductOptionSelectionRule.minRemaining(base)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
        }
    }

    /** 검증을 모두 통과한 뒤의 실제 변경 — 링크 재배치 → 흡수 옵션·그룹 감추기 → 이력 기록. */
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
            optionGroupRepository.save(target);

            mergeHistoryRepository.save(ProductOptionGroupMergeHistory.of(
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

    /**
     * 흡수 그룹의 옵션을 <b>재부모화하지 않고 각각 감춘다</b>(위 클래스 주석의 핵심 결정).
     *
     * <p>행은 남으므로 과거 주문의 {@code option_id} 참조가 끊어지지 않고, 손님 메뉴판에서는 그룹째
     * 사라지므로 표시에도 영향이 없다.
     */
    private void hideOptionsOf(ProductOptionGroup target) {
        for (ProductOption option : optionRepository.findAllByOptionGroupId(target.getProductOptionGroupId())) {
            if (!option.isVisible()) {
                continue;
            }
            option.hide();
            optionRepository.save(option);
        }
    }
}
