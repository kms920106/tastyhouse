package com.tastyhouse.ceoapi.product.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupLinkedProductResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupLinkedProductsResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionGroupResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductOptionResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionGroupQueryUseCase;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

/**
 * 점주용 옵션그룹·옵션 조회 서비스(CQRS query 측).
 *
 * <p>조회는 infra query DAO가 담당하고 이 서비스는 Result → Response 조립만 한다. write 포트는 주입하지
 * 않는다(CQRS 교차 주입 금지) — 소유권 검증은 그 포트를 내부에 감싼 협력 빈
 * {@link ShopOwnershipValidator}를 경유한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductOptionGroupQueryService implements ProductOptionGroupQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionGroupQueryService(
        ProductQueryPort productQueryPort,
        CupDepositPolicy cupDepositPolicy,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productQueryPort = productQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 가게의 옵션그룹 목록을 반환한다. <b>감춘 그룹·옵션도 포함</b>하며 그룹별 연결 메뉴 수를 함께
     * 담는다(마지막 연결이라 해제가 거부될지 화면이 미리 안내할 수 있게).
     */
    @Override
    public List<ProductOptionGroupResponse> getProductOptionGroups(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productQueryPort.findProductOptionGroupsForManagement(shopId).stream()
            .map(this::toProductOptionGroupResponse)
            .toList();
    }

    /**
     * 옵션그룹을 사용하는 메뉴 목록을 반환한다 — 연결 해제 전 영향 확인용.
     *
     * <p><b>소유권을 여기서 역판정한다.</b> 조회 결과의 메뉴가 모두 다른 가게 소유이거나 결과가 비면
     * 이 점주는 그 그룹에 접근할 수 없다 — 빈 결과를 "연결된 메뉴가 없다"로 응답하면 남의 가게
     * 옵션그룹의 존재 여부를 확인해 주는 통로가 된다.
     */
    @Override
    public List<ProductOptionGroupLinkedProductResponse> getLinkedProducts(
        Long ceoId,
        Long shopId,
        Long optionGroupId
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductOptionGroupLinkedProductResult> linked =
            productQueryPort.findLinkedProductsByOptionGroupId(optionGroupId);
        boolean ownedByRequestedShop = linked.stream().anyMatch(row -> shopId.equals(row.shopId()));
        if (!ownedByRequestedShop) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }

        // 단일 가게 불변식이 있으니 전부 같은 가게지만, 그래도 요청 가게로 한 번 더 좁힌다 — 불변식이
        // 깨진 데이터가 있어도 남의 가게 메뉴명이 응답으로 새지 않게 한다.
        return linked.stream()
            .filter(row -> shopId.equals(row.shopId()))
            .map(this::toLinkedProductResponse)
            .toList();
    }

    /**
     * 가게 옵션그룹 전체의 연결 메뉴 목록을 <b>한 번의 조회</b>로 반환한다 — 옵션그룹 연결 다이얼로그가
     * 후보 그룹마다 {@link #getLinkedProducts}를 개별 호출하던 N+1을 없앤다({@code playwright-issue-v2.md}
     * 이슈 5).
     */
    @Override
    public List<ProductOptionGroupLinkedProductsResponse> getLinkedProductsByShop(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Map<Long, List<ProductOptionGroupLinkedProductResult>> linkedByGroupId =
            productQueryPort.findLinkedProductsByShop(shopId);

        return linkedByGroupId.entrySet().stream()
            .map(entry -> toLinkedProductsResponse(entry.getKey(), entry.getValue()))
            .toList();
    }

    private ProductOptionGroupResponse toProductOptionGroupResponse(ProductOptionGroupManagementResult row) {
        return ProductOptionGroupResponse.from(
            row.id(),
            row.name(),
            row.description(),
            row.required(),
            row.multipleSelect(),
            row.minSelect(),
            row.maxSelect(),
            row.sort(),
            row.visible(),
            row.groupType(),
            row.linkedProductCount(),
            row.options().stream().map(this::toProductOptionResponse).toList()
        );
    }

    private ProductOptionResponse toProductOptionResponse(ProductOptionManagementResult row) {
        return ProductOptionResponse.from(
            row.id(),
            row.name(),
            row.additionalPrice(),
            row.sort(),
            row.visible(),
            row.cupCount(),
            // 보증금액은 저장하지 않고 조회 시점에 요율로 계산한다 — 옵션 행에는 개수만 남기기로 한
            // 결정(CupDepositPolicy 주석)의 표시 측 대응이다.
            cupDepositPolicy.depositAmountOf(row.cupCount()),
            row.personalCupDiscountAmount()
        );
    }

    private ProductOptionGroupLinkedProductResponse toLinkedProductResponse(
        ProductOptionGroupLinkedProductResult row
    ) {
        return ProductOptionGroupLinkedProductResponse.from(row.id(), row.name());
    }

    private ProductOptionGroupLinkedProductsResponse toLinkedProductsResponse(
        Long optionGroupId,
        List<ProductOptionGroupLinkedProductResult> rows
    ) {
        return ProductOptionGroupLinkedProductsResponse.from(
            optionGroupId,
            rows.stream().map(this::toLinkedProductResponse).toList()
        );
    }
}
