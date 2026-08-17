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
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴·옵션의 품절·숨김 전이와 부분실패 제약의 <b>단일 소유자</b>.
 *
 * <p>프레임워크-프리 순수 POJO다. 빈 등록은 infrastructure-module의 {@code ProductDomainConfig}가
 * {@code @Bean} 팩토리 메서드로 수행한다.
 *
 * <p>이 제약들이 api 모듈이 아니라 도메인에 있는 이유: 노출 메뉴 ≥1 · 추천 메뉴 ≥1 · 옵션
 * {@code minSelect} 잔여 개수는 애그리거트 불변식이고, ceo/admin 두 모듈에 흩어지면 한쪽만 고쳐진다.
 *
 * <p><b>부분실패 판정은 "요청 전체를 반영한 뒤의 최종 상태" 기준이다.</b> 하나씩 순차로 검사하면
 * 요청 배열의 순서에 따라 결과가 갈린다 — 노출 메뉴가 2개일 때 둘 다 숨김 요청하면 순차 검사는 첫 건을
 * 통과시키고 두 번째만 실패시키지만, 어느 것이 통과할지가 배열 순서에 좌우된다. 최종 상태 기준이면
 * "노출 메뉴가 0개가 되므로 마지막 1개는 남긴다"는 판정이 결정적이다.
 *
 * <p><b>이 서비스는 shop 컨텍스트를 참조하지 않는다.</b> 품절 기간 기본값("익일 가게 오픈 시간") 산출은
 * {@code ShopNextOpenTimeCalculator}(shop 컨텍스트)가 담당하고, ceo-api의 command service가 두 서비스를
 * 각각 주입해 조립한다 — {@code ShopBusinessHour}를 직접 참조하면 컨텍스트 경계 위반이다.
 */
public class ProductAvailabilityService {

    /** 품절 기간의 하한 — 현재 시각 +30분. 즉시 해제되는 무의미한 기간을 막는다. */
    private static final long MIN_SOLD_OUT_MINUTES = 30L;

    /** 품절 기간의 상한 — 현재 시각 +7일. */
    private static final long MAX_SOLD_OUT_DAYS = 7L;

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductCommonOptionGroupRepository productCommonOptionGroupRepository;

    public ProductAvailabilityService(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductCommonOptionGroupRepository productCommonOptionGroupRepository
    ) {
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productCommonOptionRepository = productCommonOptionRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productCommonOptionGroupRepository = productCommonOptionGroupRepository;
    }

    /**
     * 품절 기간의 유효 범위를 검증한다 — 현재 시각 +30분 이후 ~ 최대 7일 이내.
     *
     * <p>위반은 요청 전체 거부(400)다. 개별 항목이 아니라 요청 파라미터의 문제이므로 부분실패가 아니다.
     * {@code soldOutUntil}이 {@code null}이면(기본값 위임) 검증 대상이 아니다.
     */
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

    // ── 메뉴 ────────────────────────────────────────────────────────────────────────

    /**
     * 메뉴를 일괄 숨김 처리한다.
     *
     * <p>부분실패 제약: 가게 메뉴판에 노출 메뉴가 최소 1개, 사장님 추천 메뉴가 최소 1개 남아야 한다.
     * 제약에 걸리면 {@code sort} 오름차순의 <b>뒤에서부터</b> 필요한 개수만 실패로 되돌린다 —
     * 앞선 메뉴(노출 우선순위가 높은 메뉴)를 남기는 편이 점주 기대에 가깝다.
     */
    public ProductAvailabilityChangeResult hideProducts(ShopId shopId, List<ProductId> productIds) {
        LoadedProducts loaded = loadProducts(shopId, productIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        // 이미 숨김인 대상은 카운트를 줄이지 않는다(멱등) — 최종 상태 계산에서 제외한다.
        List<Product> candidates = loaded.found().stream()
            .filter(Product::isVisible)
            .sorted(Comparator.comparing(Product::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        List<Product> alreadyHidden = loaded.found().stream()
            .filter(product -> !product.isVisible())
            .toList();

        // 최종 상태 기준 판정: 요청을 전부 적용했다고 가정한 뒤 남는 개수를 본다.
        //
        // 두 제약(노출 ≥1 · 추천 ≥1)의 부족분을 <b>둘 다 원본 후보 집합 기준으로 먼저 계산</b>한다 —
        // 한쪽을 먼저 반영해 후보를 줄인 뒤 다른 쪽을 계산하면, 앞선 패스가 비추천 메뉴를 되돌렸을 때
        // 추천 부족분이 그대로 남아 같은 요청에서 두 건이 실패한다(추천 메뉴를 남기면 두 제약이 함께
        // 충족되는데도 아무것도 숨기지 못한다). 그래서 판정은 한 번, 되돌리기도 한 번만 한다.
        long visibleShortfall =
            Math.max(0, 1 - (productRepository.countVisibleByShopId(shopId) - candidates.size()));
        long representativeTargets = candidates.stream().filter(Product::isRepresentative).count();
        long representativeShortfall =
            Math.max(0, 1 - (productRepository.countVisibleRepresentativeByShopId(shopId) - representativeTargets));

        // 추천 메뉴를 되돌리면 노출 부족분도 함께 해소되므로(추천 메뉴 역시 노출 메뉴다),
        // 추천 쪽을 먼저 확정하고 남은 노출 부족분만 추가로 되돌린다.
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
        // 이미 숨김이던 대상도 성공으로 본다 — 요청한 상태에 도달해 있으므로 실패가 아니다.
        alreadyHidden.forEach(product -> succeeded.add(product.getId()));

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    /**
     * 메뉴를 일괄 품절 처리한다.
     *
     * <p>기간 유효성만 검증한다 — 품절은 목록에서 사라지지 않고 '품절' 표시로 노출되므로,
     * 숨김과 달리 메뉴판이 비는 제약이 없다.
     *
     * @param soldOutUntil 자동해제 시각. {@code null}이면 무기한 품절(수동 해제까지 유지)
     */
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

    /**
     * 메뉴의 품절을 일괄 해제한다. 해제 방향이므로 제약이 없다.
     */
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

    /**
     * 메뉴의 품절·숨김을 일괄 해제한다.
     *
     * <p>이미 판매중·노출중인 항목이 섞여 있어도 실패가 아니다(멱등). 해제 방향에는 제약이 없다.
     */
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

    /**
     * 메뉴의 품절 기간을 일괄 변경한다.
     *
     * <p>품절 상태가 아닌 대상은 요청 전체를 거부하지 않고 {@code failed}에 담는다 —
     * 목록을 열어둔 사이 다른 탭에서 해제됐을 수 있다.
     */
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

    // ── 옵션 ────────────────────────────────────────────────────────────────────────

    /**
     * 옵션을 일괄 품절 처리한다.
     *
     * <p>부분실패 제약: 옵션그룹별로 {@code minSelect} 개수만큼은 판매 중이어야 한다.
     */
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

    /**
     * 옵션을 일괄 숨김 처리한다.
     *
     * <p>숨김도 선택 불가로 만들므로 품절과 동일하게 옵션그룹별 {@code minSelect} 제약을 적용한다.
     */
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

    /**
     * 옵션의 품절·숨김을 일괄 해제한다. 해제 방향이므로 제약이 없다.
     */
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

    /**
     * 옵션의 품절 기간을 일괄 변경한다. 품절 상태가 아닌 대상은 {@code failed}에 담는다.
     */
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

    // ── 로딩·판정 헬퍼 ──────────────────────────────────────────────────────────────

    /**
     * 대상 메뉴를 한 번에 로드하고, 소유 가게 불일치·미존재 id를 먼저 실패로 분류한다.
     */
    private LoadedProducts loadProducts(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, distinctIds);

        Map<Long, Product> byId = new LinkedHashMap<>();
        found.forEach(product -> byId.put(product.getId(), product));

        List<ProductAvailabilityFailure> failed = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            if (!byId.containsKey(productId.value())) {
                // 미존재와 타 가게 소유를 같은 코드로 묶는다 — 남의 가게 메뉴의 존재 여부를 알려주지 않는다.
                failed.add(ProductAvailabilityFailure.of(productId.value(), null, ErrorCode.PRODUCT_NOT_FOUND));
            }
        }
        return new LoadedProducts(List.copyOf(byId.values()), failed);
    }

    /**
     * 대상 옵션을 두 갈래로 로드하고, 미존재·타 가게 소유를 실패로 분류한다.
     *
     * <p><b>소유권 재확인이 이 메서드의 핵심이다.</b> 옵션은 {@code option_group_id → product_id →
     * shop_id}로 두 단계 역조회가 필요하므로, body의 {@code shopId}로 가게 소유권을 확인했더라도
     * 대상 옵션이 그 가게에 속하는지 반드시 다시 확인해야 한다 — 빠뜨리면 남의 가게 옵션을 품절 처리할 수 있다.
     */
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

        // 소유 가게 판정 — 옵션그룹 → 상품 → 가게로 역조회한다.
        Map<Long, ProductOptionGroup> optionGroups = loadOptionGroups(options);
        Map<Long, ProductCommonOptionGroup> commonGroups = loadCommonOptionGroups(commonOptions);
        Map<Long, ShopId> productShopIds = loadProductShopIds(optionGroups, commonGroups);

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
            if (group == null || notOwnedBy(shopId, productShopIds, group.getProductId().value())) {
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
            if (group == null || notOwnedBy(shopId, productShopIds, group.getProductId().value())) {
                failed.add(ProductAvailabilityFailure.of(
                    option.getId(), option.getName(), ErrorCode.PRODUCT_NOT_FOUND));
                continue;
            }
            ownedCommonOptions.add(option);
        }

        return new LoadedOptions(ownedOptions, ownedCommonOptions, optionGroups, commonGroups, failed);
    }

    /**
     * 옵션그룹별 {@code minSelect} 잔여 판매중 개수를 최종 상태 기준으로 판정해, 막을 수 있는 옵션만 고른다.
     *
     * <p>판정식은 그룹별로 {@code 현재 선택가능 옵션 수 - 이번에 막을 개수 >= max(minSelect, 1)}이다.
     * {@code minSelect}가 {@code null}이거나 0이면 하한을 1로 본다 — 옵션그룹이 통째로 선택 불가가 되는 것을 막는다.
     */
    private OptionBlockPlan planBlockableOptions(
        LoadedOptions loaded,
        List<ProductAvailabilityFailure> failed,
        java.util.function.Predicate<ProductOption> blockable,
        java.util.function.Predicate<ProductCommonOption> commonBlockable
    ) {
        List<Long> alreadyInTargetState = new ArrayList<>();

        // 이미 목표 상태인 대상은 카운트를 줄이지 않는다(멱등).
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

        // 일반 옵션: optionGroupId로 묶어 그룹별로 판정한다.
        Map<Long, List<ProductOption>> byGroup = groupBy(targets, option -> option.getOptionGroupId().value());
        for (Map.Entry<Long, List<ProductOption>> entry : byGroup.entrySet()) {
            ProductOptionGroup group = loaded.optionGroups().get(entry.getKey());
            int minRemaining = minRemaining(group == null ? null : group.getMinSelect());
            List<ProductOption> groupTargets = sortedBySort(entry.getValue(), ProductOption::getSort);

            long selectable = productOptionRepository
                .findAllByOptionGroupId(ProductOptionGroupId.of(entry.getKey())).stream()
                .filter(option -> !option.isSoldOut() && option.isVisible())
                .count();

            int shortfall = (int) Math.max(0, minRemaining - (selectable - groupTargets.size()));
            for (int i = 0; i < groupTargets.size(); i++) {
                // sort 오름차순 뒤에서부터 실패로 되돌린다(앞선 옵션을 남긴다).
                if (i >= groupTargets.size() - shortfall) {
                    ProductOption rejected = groupTargets.get(i);
                    failed.add(ProductAvailabilityFailure.of(rejected.getId(), rejected.getName(),
                        ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION));
                } else {
                    allowed.add(groupTargets.get(i));
                }
            }
        }

        // 공통 옵션: 별도 테이블이므로 자기 그룹의 옵션만 센다.
        Map<Long, List<ProductCommonOption>> commonByGroup =
            groupBy(commonTargets, option -> option.getOptionGroupId().value());
        for (Map.Entry<Long, List<ProductCommonOption>> entry : commonByGroup.entrySet()) {
            ProductCommonOptionGroup group = loaded.commonOptionGroups().get(entry.getKey());
            int minRemaining = minRemaining(group == null ? null : group.getMinSelect());
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
                        ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION));
                } else {
                    allowedCommon.add(groupTargets.get(i));
                }
            }
        }

        return new OptionBlockPlan(allowed, allowedCommon, alreadyInTargetState);
    }

    /**
     * 제약 위반 개수만큼 {@code sort} 오름차순의 <b>뒤에서부터</b> 실패로 기록한다 — 앞선 메뉴(노출
     * 우선순위가 높은 메뉴)를 남기는 편이 점주 기대에 가깝다.
     *
     * <p>{@code predicate}는 되돌릴 후보를 한정한다(추천 메뉴 제약은 추천 메뉴만 되돌린다).
     *
     * <p><b>후보 목록을 변경하지 않고 {@code rejected} 맵에만 기록한다</b> — 두 제약을 각각 계산한 뒤
     * 한 번에 반영해야 하므로, 앞선 호출이 후보를 줄여 뒤 호출의 판정을 바꾸면 안 된다. id를 키로
     * 쓰므로 같은 대상이 두 번 기록되지 않고, 그 결과 {@code succeeded}와 {@code failed}가 항상 분리된다.
     */
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

    /**
     * 옵션그룹이 가리키는 상품들의 소유 가게를 한 번에 로드한다.
     */
    private Map<Long, ShopId> loadProductShopIds(
        Map<Long, ProductOptionGroup> optionGroups,
        Map<Long, ProductCommonOptionGroup> commonGroups
    ) {
        List<ProductId> productIds = new ArrayList<>();
        optionGroups.values().forEach(group -> productIds.add(group.getProductId()));
        commonGroups.values().forEach(group -> productIds.add(group.getProductId()));

        Map<Long, ShopId> shopIdByProductId = new LinkedHashMap<>();
        for (ProductId productId : distinct(productIds)) {
            productRepository.findById(productId)
                .ifPresent(product -> shopIdByProductId.put(product.getId(), product.getShopId()));
        }
        return shopIdByProductId;
    }

    private boolean notOwnedBy(ShopId shopId, Map<Long, ShopId> productShopIds, Long productId) {
        ShopId owner = productShopIds.get(productId);
        return owner == null || !owner.equals(shopId);
    }

    /** {@code minSelect}가 null이거나 0이면 하한을 1로 본다. */
    private int minRemaining(Integer minSelect) {
        return minSelect == null ? 1 : Math.max(minSelect, 1);
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

    /** 로드된 메뉴와, 미존재·타 가게 소유로 먼저 실패 처리된 대상. */
    private record LoadedProducts(
        List<Product> found,
        List<ProductAvailabilityFailure> failed
    ) {
    }

    /** 로드된 옵션 두 갈래와 그 옵션그룹, 그리고 먼저 실패 처리된 대상. */
    private record LoadedOptions(
        List<ProductOption> options,
        List<ProductCommonOption> commonOptions,
        Map<Long, ProductOptionGroup> optionGroups,
        Map<Long, ProductCommonOptionGroup> commonOptionGroups,
        List<ProductAvailabilityFailure> failed
    ) {
    }

    /** {@code minSelect} 판정을 통과해 실제로 막을 옵션과, 이미 목표 상태였던 대상. */
    private record OptionBlockPlan(
        List<ProductOption> options,
        List<ProductCommonOption> commonOptions,
        List<Long> alreadyInTargetState
    ) {
    }
}
