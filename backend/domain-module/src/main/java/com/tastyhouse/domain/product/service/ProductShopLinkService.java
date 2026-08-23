package com.tastyhouse.domain.product.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.repository.ProductShopLinkRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 ↔ 가게 연결의 불변식 오케스트레이션.
 *
 * <p>{@code Product}·{@code ProductShopLink}·{@code ProductCategory} 세 애그리거트를 한 트랜잭션에서
 * 함께 읽고 쓰므로 도메인 서비스에 둔다 — 메뉴그룹이 그 가게의 것인지, 링크가 최소 1개 남는지는
 * 모델 하나로 판정할 수 없다.
 *
 * <p><b>소유 가게 검증은 이 서비스가 하지 않는다.</b> "이 점주가 이 가게를 갖고 있는가"는 ceo-api의
 * 인가 관심사({@code ShopOwnershipValidator})이고, 도메인은 {@code ceoId}를 알지 못한다. 호출부가
 * 소유가 확인된 가게 집합을 넘겨주면 이 서비스는 그 안에 있는지만 대조한다.
 *
 * <p><b>{@code PRODUCT.shop_id}는 건드리지 않는다.</b> 그 컬럼은 원본 소유 가게로 남아 메뉴명 중복
 * 검사·옵션그룹 소유권 판정의 기준을 계속 제공한다. 이 서비스는 노출 범위(링크)만 다룬다.
 */
public class ProductShopLinkService {

    private final ProductRepository productRepository;
    private final ProductShopLinkRepository productShopLinkRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductShopLinkService(
        ProductRepository productRepository,
        ProductShopLinkRepository productShopLinkRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        this.productRepository = productRepository;
        this.productShopLinkRepository = productShopLinkRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * 메뉴의 가게 연결을 <b>통째로 교체</b>한다(PUT). 목록에 없는 가게는 연결 해제된다.
     *
     * <p>전체 교체인 이유는 "링크 1개 이상 유지"·"가게별 메뉴그룹 필수" 같은 규칙이 <b>목록 전체를 봐야
     * 판정</b>되기 때문이다. 행 단위로 열면 중간 상태가 반드시 규칙을 위반한다({@code ProductPrice}의
     * 가격 교체와 같은 판단).
     *
     * <p>기존 링크는 <b>지웠다 다시 만들지 않고 갱신</b>한다 — 지우고 새로 만들면 그 가게 메뉴판에서의
     * 표시 순서가 매번 초기화된다.
     *
     * @param ownedShopIds 호출부(ceo-api)가 소유를 확인한 가게 집합. 이 밖의 가게는 거절한다
     */
    public void replaceLinks(
        ProductId productId,
        List<ProductShopLinkSpec> specs,
        Set<Long> ownedShopIds
    ) {
        Product product = loadProduct(productId);

        if (specs == null || specs.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        Map<Long, ProductShopLinkSpec> requested = toDistinctSpecsByShopId(specs);
        for (ProductShopLinkSpec spec : requested.values()) {
            validateOwned(spec.shopId(), ownedShopIds);
            validateCategory(ShopId.of(spec.shopId()), spec.productCategoryId());
        }

        Map<Long, ProductShopLink> existing = new LinkedHashMap<>();
        for (ProductShopLink link : productShopLinkRepository.findAllByProductId(productId)) {
            existing.put(link.getShopId().value(), link);
        }

        // 해제 대상: 기존에 있었으나 요청에 없는 가게. 그 가게 메뉴판이 비게 되면 거절한다.
        for (Map.Entry<Long, ProductShopLink> entry : existing.entrySet()) {
            if (requested.containsKey(entry.getKey())) {
                continue;
            }
            validateShopKeepsVisibleProduct(product, ShopId.of(entry.getKey()));
            productShopLinkRepository.delete(entry.getValue());
        }

        for (ProductShopLinkSpec spec : requested.values()) {
            ProductShopLink link = existing.get(spec.shopId());
            ProductCategoryId categoryId = ProductCategoryId.of(spec.productCategoryId());
            if (link == null) {
                ShopId targetShopId = ShopId.of(spec.shopId());
                productShopLinkRepository.save(
                    ProductShopLink.of(productId, targetShopId, categoryId, nextSort(targetShopId))
                );
                continue;
            }
            // 기존 링크는 메뉴그룹만 갱신하고 표시 순서는 유지한다 — 그 가게 메뉴판에서의 위치를
            // 다른 가게의 연결 변경이 흔들어서는 안 된다.
            link.relocate(categoryId, link.getSort());
            productShopLinkRepository.save(link);
        }
    }

    /**
     * 가게 메뉴판에 메뉴를 <b>불러온다</b>(가게 기준 진입 — "메뉴판 편집 → 메뉴 불러오기").
     *
     * <p>{@link #replaceLinks}가 메뉴 기준이라면 이쪽은 가게 기준으로 한 건만 더한다. 이미 연결된
     * 가게면 {@code PRODUCT_SHOP_LINK_ALREADY_LINKED}로 거절한다 — 조용히 통과시키면 화면이
     * "불러왔다"고 표시하지만 메뉴그룹은 이전 값 그대로여서 결과가 어긋난다.
     */
    public void linkToShop(ProductId productId, ShopId targetShopId, Long productCategoryId) {
        loadProduct(productId);
        validateCategory(targetShopId, productCategoryId);

        if (productShopLinkRepository.existsByProductIdAndShopId(productId, targetShopId)) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_ALREADY_LINKED);
        }

        productShopLinkRepository.save(
            ProductShopLink.of(productId, targetShopId, ProductCategoryId.of(productCategoryId), nextSort(targetShopId))
        );
    }

    /**
     * 가게 메뉴판에서 메뉴를 <b>제외</b>한다 — 링크만 지운다.
     *
     * <p>메뉴 자체는 삭제되지 않는다. 다른 가게에 연결돼 있으면 그쪽에는 그대로 노출된다.
     *
     * <p>마지막 링크는 해제할 수 없다 — 링크가 0개가 되면 어느 메뉴판에도 없으면서 삭제되지도 않은
     * 유령 메뉴가 된다. 그 경우 제외가 아니라 메뉴 삭제를 써야 한다.
     */
    public void unlinkFromShop(ProductId productId, ShopId targetShopId) {
        Product product = loadProduct(productId);

        ProductShopLink link = productShopLinkRepository.findByProductIdAndShopId(productId, targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_SHOP_LINK_NOT_FOUND));

        if (productShopLinkRepository.countByProductId(productId) <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        validateShopKeepsVisibleProduct(product, targetShopId);
        productShopLinkRepository.delete(link);
    }

    /**
     * 메뉴 등록 시의 <b>추가</b> 가게 연결을 만든다.
     *
     * <p><b>원본 소유 가게 링크는 여기서 만들지 않는다</b> — 메뉴를 만드는 경로가 셋(ceo 등록·admin
     * 등록·batch BBQ 동기화)이라 호출부마다 배선하면 한 곳이 반드시 빠지므로,
     * {@code ProductRegistrationService#createProduct}가 저장과 같은 자리에서 함께 만든다.
     * 이 메서드는 그 위에 <b>여러 가게 지정을 얹는 ceo 경로 전용</b>이다.
     *
     * <p>{@code specs}가 비면 아무것도 하지 않는다 — 원본 가게 단일 연결 상태 그대로이며, 이것이
     * 기존 단일 가게 등록 동작이 완전히 보존되는 지점이다(링크 1개 메뉴는 동작이 그대로라는 안전장치).
     *
     * <p>이미 만들어진 원본 가게 링크가 목록에 함께 실려 와도 중복 저장하지 않고 건너뛴다.
     */
    public void createInitialLinks(
        ProductId productId,
        List<ProductShopLinkSpec> specs,
        Set<Long> ownedShopIds
    ) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        Map<Long, ProductShopLinkSpec> requested = toDistinctSpecsByShopId(specs);
        for (ProductShopLinkSpec spec : requested.values()) {
            validateOwned(spec.shopId(), ownedShopIds);
            ShopId targetShopId = ShopId.of(spec.shopId());
            validateCategory(targetShopId, spec.productCategoryId());

            if (productShopLinkRepository.existsByProductIdAndShopId(productId, targetShopId)) {
                // 등록 시 이미 만들어진 원본 가게 링크다. 중복 저장하면 UNIQUE 제약에 걸린다.
                continue;
            }
            productShopLinkRepository.save(ProductShopLink.of(
                productId, targetShopId, ProductCategoryId.of(spec.productCategoryId()), nextSort(targetShopId)
            ));
        }
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 같은 가게가 두 번 실려 오면 뒤엣것으로 접는다. 거절하지 않는 이유는 화면의 토글 목록이 중복을
     * 만들 수 있는 형태이고, 두 항목의 메뉴그룹이 같다면 사용자 의도가 모호하지 않기 때문이다.
     */
    private Map<Long, ProductShopLinkSpec> toDistinctSpecsByShopId(List<ProductShopLinkSpec> specs) {
        Map<Long, ProductShopLinkSpec> distinct = new LinkedHashMap<>();
        for (ProductShopLinkSpec spec : specs) {
            if (spec.shopId() == null) {
                throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
            }
            distinct.put(spec.shopId(), spec);
        }
        return distinct;
    }

    private void validateOwned(Long shopId, Set<Long> ownedShopIds) {
        if (ownedShopIds == null || !ownedShopIds.contains(shopId)) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
        }
    }

    /**
     * 메뉴그룹이 지정됐는지, 그리고 그것이 <b>그 가게의</b> 메뉴그룹인지 확인한다.
     *
     * <p>가게 대조를 빠뜨리면 남의 가게 메뉴그룹 id를 실어 보내 그 가게 메뉴판 구조를 들여다볼 수 있다.
     */
    private void validateCategory(ShopId shopId, Long productCategoryId) {
        if (productCategoryId == null) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_REQUIRED);
        }

        ProductCategory category = productCategoryRepository.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH));

        if (!shopId.equals(category.getShopId())) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH);
        }
    }

    /**
     * 연결을 해제해도 그 가게 메뉴판에 노출 메뉴가 남는지 확인한다.
     *
     * <p>숨김 상태인 메뉴를 제외하는 것은 노출 수를 줄이지 않으므로 제약 대상이 아니다 — 이 판정을
     * 빠뜨리면 이미 숨겨진 메뉴를 빼는 것조차 거절돼 점주가 메뉴판을 정리할 수 없다.
     */
    private void validateShopKeepsVisibleProduct(Product product, ShopId shopId) {
        if (!product.isVisible()) {
            return;
        }
        if (productRepository.countVisibleByShopId(shopId) <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        }
    }

    /**
     * 대상 가게 메뉴판의 <b>끝 순서</b>를 구한다. 새 연결은 항상 끝에 붙는다 — 다른 가게에서의 순서를
     * 가져오면 그 가게 메뉴판의 기존 배열을 헤집는다.
     */
    private Integer nextSort(ShopId shopId) {
        List<ProductShopLink> links = productShopLinkRepository.findAllByShopId(shopId);
        int max = -1;
        for (ProductShopLink link : links) {
            if (link.getSort() != null && link.getSort() > max) {
                max = link.getSort();
            }
        }
        return max + 1;
    }
}
