package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 ↔ 옵션그룹 연결(N:M)의 단일 소유자.
 *
 * <p><b>핵심 불변식 — 옵션그룹은 단일 가게에만 속한다.</b> 연결 시 대상 메뉴의 가게와 그 그룹의
 * 기존 연결 메뉴들의 가게가 다르면 {@code PRODUCT_OPTION_GROUP_SHOP_MISMATCH}(400)로 거부한다.
 *
 * <p>이 불변식 덕분에 소유권 판정에서 <b>ANY/ALL 구분이 사라진다</b> — "연결된 메뉴 중 아무거나
 * 하나"의 가게가 곧 그룹의 가게다. 이 규칙이 없으면 "그룹이 A가게 메뉴와 B가게 메뉴에 동시에
 * 연결됐을 때 누가 이 그룹을 품절 처리할 수 있는가"가 답이 없는 질문이 된다.
 *
 * <p><b>마지막 연결 해제는 막는다</b>({@code PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK}) —
 * 연결이 0건이면 어디서도 보이지 않는 고아 그룹이 되어 되찾을 UI가 없다.
 */
public class ProductOptionGroupLinkService {

    private final ProductOptionGroupLinkRepository linkRepository;
    private final ProductRepository productRepository;

    public ProductOptionGroupLinkService(
        ProductOptionGroupLinkRepository linkRepository,
        ProductRepository productRepository
    ) {
        this.linkRepository = linkRepository;
        this.productRepository = productRepository;
    }

    /**
     * 메뉴에 옵션그룹을 연결한다. 이미 연결돼 있으면 아무 일도 하지 않는다(멱등).
     *
     * <p>{@code sort}는 이 메뉴에서의 기존 연결 개수(= 맨 뒤)로 부여한다.
     */
    public void link(ProductId productId, ProductOptionGroupId optionGroupId) {
        if (linkRepository.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        validateSameShop(productId, optionGroupId);

        int nextSort = linkRepository.findAllByProductId(productId).size();
        linkRepository.save(ProductOptionGroupLink.of(productId, optionGroupId, nextSort));
    }

    /**
     * 연결을 해제한다. 마지막 연결이면 거부한다.
     */
    public void unlink(ProductId productId, ProductOptionGroupId optionGroupId) {
        ProductOptionGroupLink link = linkRepository
            .findByProductIdAndOptionGroupId(productId, optionGroupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));

        if (linkRepository.findAllByOptionGroupId(optionGroupId).size() <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK);
        }

        linkRepository.delete(link);
        renumber(productId);
    }

    /**
     * 이 메뉴에서의 옵션그룹 순서를 통째로 교체한다.
     *
     * <p>{@code sort} 값을 받지 않고 순서 있는 id 배열만 받아 {@code 0..N-1}을 부여한다
     * ({@code ProductSortService}와 같은 replace-all 원칙).
     */
    public void reorder(ProductId productId, List<ProductOptionGroupId> orderedGroupIds) {
        List<ProductOptionGroupLink> current = linkRepository.findAllByProductId(productId);
        Map<Long, ProductOptionGroupLink> byGroupId = current.stream()
            .collect(Collectors.toMap(link -> link.getOptionGroupId().value(), Function.identity()));

        List<Long> requested = distinctRawIds(orderedGroupIds);
        if (byGroupId.size() != requested.size() || !byGroupId.keySet().containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOptionGroupLink link = byGroupId.get(requested.get(index));
            link.changeSort(index);
            linkRepository.save(link);
        }
    }

    /**
     * 옵션그룹 합치기에서 링크를 흡수 그룹 → 기준 그룹으로 옮긴다.
     *
     * <p>이 메서드가 {@code link}/{@code unlink}와 별개로 존재하는 이유는 셋이다.
     * <ul>
     *   <li>{@code unlink}는 <b>마지막 연결을 막는다</b> — 합치기에서는 흡수 그룹의 연결이 전부
     *       사라지는 것이 정상이므로 그 가드를 통과할 수 없다.</li>
     *   <li>{@code link}는 sort를 맨 뒤로 붙인다 — 합치기는 <b>원래 위치를 보존</b>해야 손님 메뉴판의
     *       옵션그룹 순서가 흔들리지 않는다.</li>
     *   <li>{@code UNIQUE (product_id, option_group_id)} 충돌 처리와 sort 재정규화가 이 클래스의
     *       불변식이므로 {@code private renumber}를 밖으로 공개하지 않아도 된다.</li>
     * </ul>
     *
     * <p><b>기준 그룹이 이미 그 메뉴에 연결돼 있으면 흡수 링크를 삭제만 한다</b> — 옮기면 UNIQUE에
     * 걸린다. 다만 합치기 검증({@code MERGE_SAME_PRODUCT_LINKED})이 그 상태를 미리 막으므로 실제로는
     * 도달하지 않으며, 여기서는 방어적으로만 다룬다.
     *
     * <p>마지막에 <b>영향받은 메뉴만</b> sort를 {@code 0..N-1}로 재정규화해 구멍을 없앤다.
     */
    public void relink(
        List<ProductId> productIds,
        ProductOptionGroupId fromOptionGroupId,
        ProductOptionGroupId toOptionGroupId
    ) {
        Set<Long> affectedProductIds = new LinkedHashSet<>();

        for (ProductId productId : productIds) {
            ProductOptionGroupLink link = linkRepository
                .findByProductIdAndOptionGroupId(productId, fromOptionGroupId)
                .orElse(null);
            if (link == null) {
                continue;
            }
            affectedProductIds.add(productId.value());

            Integer preservedSort = link.getSort();
            linkRepository.delete(link);

            if (!linkRepository.existsByProductIdAndOptionGroupId(productId, toOptionGroupId)) {
                linkRepository.save(ProductOptionGroupLink.of(productId, toOptionGroupId, preservedSort));
            }
        }

        affectedProductIds.forEach(productId -> renumber(ProductId.of(productId)));
    }

    /**
     * 옵션그룹이 속한 가게를 역조회한다 — "그룹 → 링크 → 메뉴 → 가게" 3단.
     *
     * <p>단일 가게 불변식 덕분에 "연결된 아무 메뉴 하나"로 판정할 수 있다. 연결이 0건이면
     * {@code null}(소유자 없음)이며, 호출부는 이를 "접근 불가"로 다뤄야 한다.
     */
    public ShopId findOwningShopId(ProductOptionGroupId optionGroupId) {
        return linkRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductOptionGroupLink::getProductId)
            .map(productRepository::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(Product::getShopId)
            .findFirst()
            .orElse(null);
    }

    /**
     * 단일 가게 불변식을 검증한다 — 그룹의 기존 연결 가게와 대상 메뉴의 가게가 같아야 한다.
     */
    private void validateSameShop(ProductId productId, ProductOptionGroupId optionGroupId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        ShopId owner = findOwningShopId(optionGroupId);
        if (owner != null && !owner.equals(product.getShopId())) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
        }
    }

    /** 해제 후 남은 연결의 sort를 0..N-1로 다시 매긴다. */
    private void renumber(ProductId productId) {
        List<ProductOptionGroupLink> remaining = linkRepository.findAllByProductId(productId);
        for (int index = 0; index < remaining.size(); index++) {
            ProductOptionGroupLink link = remaining.get(index);
            link.changeSort(index);
            linkRepository.save(link);
        }
    }

    private List<Long> distinctRawIds(List<ProductOptionGroupId> ids) {
        if (ids == null) {
            return List.of();
        }
        Set<Long> raw = new LinkedHashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(id -> raw.add(id.value()));
        return new ArrayList<>(raw);
    }
}
