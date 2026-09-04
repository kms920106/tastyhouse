package com.tastyhouse.ceoapplication.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.product.port.in.ProductCategoryReorderCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductRelocateCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductReorderCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductSortCommandUseCase;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.service.ProductSortService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴그룹·메뉴 순서 변경 서비스(CQRS command 측).
 *
 * <p><b>{@code sort} 값을 클라이언트에서 받지 않는다</b> — 순서 있는 id 배열만 받고 도메인 서비스
 * ({@link ProductSortService})가 배열 인덱스로 {@code 0..N-1}을 부여하므로 "sort 충돌"이라는 개념 자체가
 * 존재하지 않는다. 요청 집합이 가게의 현재 집합과 어긋나면(다른 탭에서 추가·삭제된 stale 요청)
 * {@code *_ORDER_TARGET_MISMATCH}(400)로 거부된다.
 *
 * <p>세 연산이 한 서비스인 이유는 그룹 이동이 출발·도착 두 그룹의 정렬 집합을 한 트랜잭션에서 함께
 * 재정규화하기 때문이다.
 */
@Service
@Transactional
public class ProductSortCommandService implements ProductSortCommandUseCase {

    private final ProductSortService productSortService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductSortCommandService(
        ProductSortService productSortService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productSortService = productSortService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /** 가게의 메뉴그룹 순서를 통째로 교체한다. */
    @Override
    public void reorderProductCategories(ProductCategoryReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productCategoryIds = command.productCategoryIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.reorderCategories(ShopId.of(shopId), toProductCategoryIds(productCategoryIds));
    }

    /**
     * 한 메뉴그룹 안의 메뉴 순서를 통째로 교체한다.
     *
     * <p>{@code productCategoryId}가 {@code null}이면 미분류 메뉴 목록이 대상이다.
     */
    @Override
    public void reorderProducts(ProductReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.reorderProducts(
            ShopId.of(shopId),
            toProductCategoryId(productCategoryId),
            toProductIds(productIds)
        );
    }

    /**
     * 메뉴를 다른 메뉴그룹으로 옮긴다. 도착 그룹의 최종 순서와 출발 그룹의 정렬이 함께 재정규화된다.
     */
    @Override
    public void relocateProducts(ProductRelocateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long targetProductCategoryId = command.targetProductCategoryId();
        List<Long> productIds = command.productIds();
        List<Long> targetOrderedProductIds = command.targetOrderedProductIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productSortService.relocateProducts(
            ShopId.of(shopId),
            toProductCategoryId(targetProductCategoryId),
            toProductIds(productIds),
            toProductIds(targetOrderedProductIds)
        );
    }

    // ── 변환 ────────────────────────────────────────────────────────────────────────

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }

    /**
     * 빈 목록을 스펙이 약속한 코드로 거부한다 — Bean Validation {@code @NotEmpty}가 먼저 걸러 주지만
     * 그 경로는 일반 검증 실패 응답이라 이 {@code code}가 프론트에 전달되지 않는다.
     */
    private List<ProductCategoryId> toProductCategoryIds(List<Long> productCategoryIds) {
        if (productCategoryIds == null || productCategoryIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);
        }
        return productCategoryIds.stream().map(ProductCategoryId::of).toList();
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }
        return productIds.stream().map(ProductId::of).toList();
    }
}
