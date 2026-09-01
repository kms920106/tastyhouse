package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 상품 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트) — 관리자용.
 *
 * <p>전체 상품 목록과, 점주가 올린 변경 요청(이미지·베지테리언·대표메뉴)의 검수 목록을 조회한다.
 * 회원 화면 조회는 {@link ProductQueryPort}, 점주 관리 화면 조회는 {@code ProductOwnerQueryPort}가
 * 소유한다.
 *
 * <p>{@code find*RequestPage} 3종이 이 포트의 성격을 보여준다 — 관리자는 개별 가게의 상품을 관리하는
 * 것이 아니라 승인 상태({@link ApprovalStatus})별로 요청을 <b>검수</b>한다.
 */
public interface ProductManagementQueryPort {

    PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery);

    PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(ApprovalStatus status, PageQuery pageQuery);

    PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    ProductOptionsResult findProductOptions(Long productId);

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<String> findProductImageUrls(Long productId);

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ProductDetailResult> findProductDetailById(Long productId);

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<ProductCategoryResult> findProductCategories(Long shopId);
}
