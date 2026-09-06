package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 ↔ 가게 연결(N:M) 순수 도메인 모델 — "이 메뉴가 어느 가게 메뉴판에 노출되는가".
 *
 * <p><b>선례를 그대로 따른다</b>: {@code ProductOptionGroupLink}가 이미 같은 형태의 N:M(메뉴↔옵션그룹)을
 * 링크 테이블로 표현하며, 정렬 순서를 링크가 소유하는 구조까지 동일하다.
 *
 * <p><b>★ {@code PRODUCT.shop_id}를 대체하지 않는다.</b> 그 컬럼은 <b>원본 소유 가게</b>(메뉴를 만든
 * 가게)로 남아 메뉴명 중복 검사·옵션그룹 소유권 판정의 기준으로 계속 쓰이고, 이 링크는 노출 범위만
 * 담는다. 두 관심사를 분리했기 때문에 기존 전제 대부분이 그대로 성립하며, <b>링크가 1개(원본 가게만)인
 * 메뉴는 동작이 완전히 그대로다</b> — 이 설계의 안전장치다.
 *
 * <p>{@code productCategoryId}·{@code sort}가 메뉴가 아니라 <b>링크</b>에 있는 것이 핵심이다. 같은
 * 메뉴라도 가게마다 다른 메뉴그룹에 배치되고 다른 순서로 놓일 수 있으므로, 이 둘은 메뉴의 속성이 아니라
 * 연결의 속성이다({@code ProductOptionGroupLink.sort}와 같은 판단).
 *
 * <p>품절·숨김은 <b>링크가 아니라 메뉴가 소유한다</b>({@code Product.soldOut}·{@code Product.visible}).
 * 링크로 분리하면 {@code PRODUCT}와 두 진실원이 생겨 조회 경로 전체가 바뀌므로, 메뉴 단위 유지를
 * 택했다 — 가게별 재고 분리가 필요해지면 그때 별도 과제로 다룬다.
 *
 * <p>영속화는 infrastructure-module의 {@code ProductShopLinkJpaEntity} +
 * {@code ProductShopLinkMapper}가 담당한다.
 */
public class ProductShopLink {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final ShopId shopId;
    /** 이 가게에서의 메뉴그룹. {@code null}이면 미분류다. */
    private ProductCategoryId productCategoryId;
    /** 이 가게 메뉴판에서의 표시 순서 — 가게별로 독립이다. */
    private Integer sort;

    private ProductShopLink(
        Long id,
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    public static ProductShopLink of(
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        return new ProductShopLink(null, productId, shopId, productCategoryId, sort);
    }

    public static ProductShopLink reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        return new ProductShopLink(id, productId, shopId, productCategoryId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductCategoryId getProductCategoryId() {
        return this.productCategoryId;
    }

    public Integer getSort() {
        return this.sort;
    }

    /**
     * 이 가게에서의 메뉴그룹 배치와 표시 순서를 <b>함께</b> 바꾼다.
     *
     * <p>두 전이를 따로 두지 않는 이유는 {@code Product#relocate}와 같다 — 그룹만 옮기고 순서를 남기면
     * 출발 그룹의 sort 값이 도착 그룹에 섞여 순서가 뒤엉킨다. 따로 두면 호출부가 한쪽만 부르는 실수가
     * 컴파일을 통과한다.
     */
    public void relocate(ProductCategoryId productCategoryId, Integer sort) {
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }
}
