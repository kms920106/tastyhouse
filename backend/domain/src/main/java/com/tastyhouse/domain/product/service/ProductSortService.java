package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴그룹·메뉴 정렬과 그룹 이동을 담당하는 도메인 서비스.
 *
 * <p><b>{@code sort} 값을 클라이언트에서 받지 않는다.</b> 순서 있는 id 배열만 받고 서버가 배열
 * 인덱스로 {@code 0..N-1}을 부여한다 — 그래서 "sort 충돌"이라는 개념 자체가 존재하지 않는다.
 *
 * <p>요청 id 집합이 현재 집합과 다르면 {@code *_ORDER_TARGET_MISMATCH}(400)로 거부한다. 다른 탭에서
 * 메뉴가 추가·삭제된 뒤의 stale 요청을 그대로 적용하면 빠진 메뉴의 순서가 조용히 뒤로 밀린다.
 */
public class ProductSortService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductSortService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * 가게의 메뉴그룹 순서를 통째로 교체한다.
     */
    public void reorderCategories(ShopId shopId, List<ProductCategoryId> orderedIds) {
        List<ProductCategory> current = productCategoryRepository.findAllByShopId(shopId);
        Map<Long, ProductCategory> byId = current.stream()
            .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, ErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            ProductCategory category = byId.get(requested.get(index));
            category.changeSort(index);
            productCategoryRepository.save(category);
        }
    }

    /**
     * 한 메뉴그룹(미분류 포함) 안의 메뉴 순서를 통째로 교체한다.
     *
     * <p>{@code productCategoryId}가 {@code null}이면 미분류 메뉴 목록이 대상이다.
     */
    public void reorderProducts(
        ShopId shopId,
        ProductCategoryId productCategoryId,
        List<ProductId> orderedIds
    ) {
        List<Product> current = productRepository.findAllByShopIdAndCategoryId(shopId, productCategoryId);
        Map<Long, Product> byId = current.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            Product product = byId.get(requested.get(index));
            product.changeSort(index);
            productRepository.save(product);
        }
    }

    /**
     * 메뉴를 다른 메뉴그룹으로 옮긴다.
     *
     * <p>{@code targetOrderedIds}까지 받는 이유는 드래그로 다른 그룹에 놓을 때 <b>"어느 위치에"</b>
     * 놓았는지가 함께 결정되기 때문이다. 도착 그룹의 최종 순서 전체를 받아 {@code 0..N-1}로 정규화한다.
     *
     * <p><b>출발 그룹의 sort도 함께 재정규화한다</b> — 빠져나간 자리에 구멍이 남으면 다음 재정렬
     * 요청의 집합 검증은 통과하지만 화면 순서가 실제 저장값과 어긋난다.
     */
    public void relocateProducts(
        ShopId shopId,
        ProductCategoryId targetCategoryId,
        List<ProductId> movedIds,
        List<ProductId> targetOrderedIds
    ) {
        if (movedIds == null || movedIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Long> movedRawIds = distinctRawIds(movedIds);
        List<Product> moved = productRepository.findAllByShopIdAndIdIn(shopId, movedIds);
        if (moved.size() != movedRawIds.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        // 출발 그룹들을 미리 기억한다 — 이동 후에는 알 수 없다.
        Set<Long> sourceCategoryIds = new LinkedHashSet<>();
        boolean movedFromUncategorized = false;
        for (Product product : moved) {
            if (product.getProductCategoryId() == null) {
                movedFromUncategorized = true;
            } else {
                sourceCategoryIds.add(product.getProductCategoryId().value());
            }
        }

        // 도착 그룹의 최종 순서를 0..N-1로 부여한다. 이동 대상이 그 목록에 빠짐없이 들어 있어야 한다.
        List<Long> targetRawIds = distinctRawIds(targetOrderedIds);
        if (!new LinkedHashSet<>(targetRawIds).containsAll(movedRawIds)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        List<Product> targetGroup = productRepository.findAllByShopIdAndCategoryId(shopId, targetCategoryId);
        Map<Long, Product> targetById = targetGroup.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Product> movedById = moved.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 도착 목록은 (기존 도착 그룹 ∪ 이동 대상)과 정확히 일치해야 한다.
        Set<Long> expected = new LinkedHashSet<>(targetById.keySet());
        expected.addAll(movedRawIds);
        requireSameSet(expected, targetRawIds, ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < targetRawIds.size(); index++) {
            Long rawId = targetRawIds.get(index);
            Product product = movedById.containsKey(rawId) ? movedById.get(rawId) : targetById.get(rawId);
            product.relocate(targetCategoryId, index);
            productRepository.save(product);
        }

        // 출발 그룹 재정규화 — 도착 그룹 자신은 위에서 이미 정리했으므로 건너뛴다.
        Long targetRawCategoryId = targetCategoryId == null ? null : targetCategoryId.value();
        for (Long sourceCategoryId : sourceCategoryIds) {
            if (sourceCategoryId.equals(targetRawCategoryId)) {
                continue;
            }
            renumber(shopId, ProductCategoryId.of(sourceCategoryId));
        }
        if (movedFromUncategorized && targetRawCategoryId != null) {
            renumber(shopId, null);
        }
    }

    /** 그룹에 남은 메뉴의 sort를 0..N-1로 다시 매긴다. */
    private void renumber(ShopId shopId, ProductCategoryId productCategoryId) {
        List<Product> remaining = productRepository.findAllByShopIdAndCategoryId(shopId, productCategoryId);
        for (int index = 0; index < remaining.size(); index++) {
            Product product = remaining.get(index);
            product.changeSort(index);
            productRepository.save(product);
        }
    }

    private <T> List<Long> distinctRawIds(List<T> ids) {
        if (ids == null) {
            return List.of();
        }
        List<Long> raw = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        for (T id : ids) {
            Long value = id instanceof ProductId(Long productValue) ? productValue
                : id instanceof ProductCategoryId(Long categoryValue) ? categoryValue
                : null;
            if (value != null && seen.add(value)) {
                raw.add(value);
            }
        }
        return raw;
    }

    private void requireSameSet(Set<Long> current, List<Long> requested, ErrorCode mismatchCode) {
        if (current.size() != requested.size() || !current.containsAll(requested)) {
            throw new BusinessException(mismatchCode);
        }
    }
}
