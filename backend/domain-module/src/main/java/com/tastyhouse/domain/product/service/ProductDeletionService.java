package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 일괄 삭제(소프트)와 그 부분실패 제약의 단일 소유자.
 *
 * <p><b>숨김과 같은 불변식을 적용한다</b>(노출 메뉴 ≥1 · 추천 메뉴 ≥1) — 숨김만 막고 삭제를 열어두면
 * 점주가 삭제로 우회해 빈 메뉴판을 만들 수 있다.
 *
 * <p><b>진행 중 주문이 있다고 삭제를 막지 않는다</b> — {@code ORDER_PRODUCT}가 이름·가격·이미지를
 * 주문 시점에 박제하므로 과거·진행 중 주문은 영향받지 않는다.
 *
 * <p>판정은 {@code ProductAvailabilityService#hideProducts}와 같은 <b>최종 상태 기준</b>이다 —
 * 순차 검사는 요청 배열의 순서에 따라 결과가 갈린다.
 */
public class ProductDeletionService {

    private final ProductRepository productRepository;

    public ProductDeletionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 메뉴를 일괄 소프트 삭제한다.
     *
     * <p>제약에 걸리면 {@code sort} 오름차순의 <b>뒤에서부터</b> 필요한 개수만 실패로 되돌린다 —
     * 앞선 메뉴(노출 우선순위가 높은 메뉴)를 남기는 편이 점주 기대에 가깝다.
     */
    public ProductAvailabilityChangeResult deleteProducts(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        if (distinctIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

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

        // 이미 숨김인 메뉴는 노출 카운트를 줄이지 않으므로 제약 계산에서 제외한다.
        // (삭제 자체는 숨김 여부와 무관하게 수행한다.)
        List<Product> visibleTargets = byId.values().stream()
            .filter(Product::isVisible)
            .sorted(Comparator.comparing(Product::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        long visibleShortfall =
            Math.max(0, 1 - (productRepository.countVisibleByShopId(shopId) - visibleTargets.size()));
        long representativeTargets = visibleTargets.stream().filter(Product::isRepresentative).count();
        long representativeShortfall = Math.max(0,
            1 - (productRepository.countVisibleRepresentativeByShopId(shopId) - representativeTargets));

        // 추천 메뉴를 되돌리면 노출 부족분도 함께 해소되므로(추천 메뉴 역시 노출 메뉴다),
        // 추천 쪽을 먼저 확정하고 남은 노출 부족분만 추가로 되돌린다.
        Map<Long, ProductAvailabilityFailure> rejected = new LinkedHashMap<>();
        rejectFromTail(visibleTargets, rejected, representativeShortfall,
            ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE, Product::isRepresentative);
        rejectFromTail(visibleTargets, rejected, visibleShortfall - rejected.size(),
            ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE, product -> true);

        failed.addAll(rejected.values());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : byId.values()) {
            if (rejected.containsKey(product.getId())) {
                continue;
            }
            if (product.isDeleted()) {
                // 이미 삭제된 메뉴는 요청한 상태에 도달해 있으므로 실패가 아니다(멱등).
                succeeded.add(product.getId());
                continue;
            }
            product.delete();
            productRepository.save(product);
            succeeded.add(product.getId());
        }

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    private void rejectFromTail(
        List<Product> candidates,
        Map<Long, ProductAvailabilityFailure> rejected,
        long shortfall,
        ErrorCode errorCode,
        Predicate<Product> predicate
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

    private List<ProductId> distinct(List<ProductId> ids) {
        return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
