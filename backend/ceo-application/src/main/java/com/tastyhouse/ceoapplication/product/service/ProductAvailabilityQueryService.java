package com.tastyhouse.ceoapplication.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.product.response.ProductAvailabilityGroupResponse;
import com.tastyhouse.ceoapplication.product.response.ProductAvailabilityItemResponse;
import com.tastyhouse.ceoapplication.product.response.ProductOptionAvailabilityGroupResponse;
import com.tastyhouse.ceoapplication.product.response.ProductOptionAvailabilityItemResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductAvailabilityQueryUseCase;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

/**
 * 점주용 품절·숨김 관리 목록 조회 서비스(CQRS query 측).
 *
 * <p>이 화면은 점주가 상태를 관리하는 화면이므로 <b>품절·숨김 항목도 포함해</b> 조회한다(손님 화면 쿼리와
 * 정반대). 조회는 infra query DAO가 담당하고 이 서비스는 Result → Response 조립만 한다.
 *
 * <p>페이징이 없는 이유: 품절 처리는 전체 메뉴판을 한눈에 보고 골라야 하는 작업이고, 그룹 단위 정렬과
 * "그룹 체크 시 하위 전체 선택"이 페이지 경계를 넘으면 성립하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class ProductAvailabilityQueryService implements ProductAvailabilityQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductAvailabilityQueryService(
        ProductQueryPort productQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productQueryPort = productQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 메뉴 탭 목록을 메뉴그룹(카테고리) 단위로 묶어 반환한다.
     *
     * <p>DAO가 카테고리 {@code sort} → 상품 {@code sort} 오름차순으로 이미 정렬해 주므로,
     * 조립은 등장 순서를 유지하는 {@link LinkedHashMap} 그룹핑으로 충분하다.
     */
    @Override
    public List<ProductAvailabilityGroupResponse> getProductAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductAvailabilitySearchCondition condition =
            ProductAvailabilitySearchCondition.of(shopId, keyword, soldOutOnly, hiddenOnly);
        List<ProductAvailabilityItemResult> rows = productQueryPort.findProductAvailability(condition);

        // 카테고리 미지정 메뉴(categoryId == null)도 한 묶음으로 모은다 — 화면에서 "분류 없음"으로 표시한다.
        Map<CategoryKey, List<ProductAvailabilityItemResponse>> grouped = new LinkedHashMap<>();
        for (ProductAvailabilityItemResult row : rows) {
            CategoryKey key = new CategoryKey(row.categoryId(), row.categoryName(), row.categorySort());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(toProductAvailabilityItemResponse(row));
        }

        List<ProductAvailabilityGroupResponse> response = new ArrayList<>();
        grouped.forEach((key, products) -> response.add(ProductAvailabilityGroupResponse.from(
            key.categoryId(),
            key.categoryName(),
            key.categorySort(),
            products
        )));
        return response;
    }

    /**
     * 옵션 탭 목록을 옵션그룹 단위로 반환한다. 일반 옵션그룹과 공통 옵션그룹이 하나의 목록으로 합쳐진다.
     */
    @Override
    public List<ProductOptionAvailabilityGroupResponse> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductAvailabilitySearchCondition condition =
            ProductAvailabilitySearchCondition.of(shopId, keyword, soldOutOnly, hiddenOnly);

        return productQueryPort.findProductOptionAvailability(condition).stream()
            .map(this::toProductOptionAvailabilityGroupResponse)
            .toList();
    }

    private ProductAvailabilityItemResponse toProductAvailabilityItemResponse(ProductAvailabilityItemResult row) {
        return ProductAvailabilityItemResponse.from(
            row.id(),
            row.name(),
            row.originalPrice(),
            row.discountPrice(),
            row.imageUrl(),
            row.soldOut(),
            row.soldOutUntil(),
            row.visible(),
            row.representative(),
            row.sort()
        );
    }

    private ProductOptionAvailabilityGroupResponse toProductOptionAvailabilityGroupResponse(
        ProductOptionAvailabilityGroupResult group
    ) {
        List<ProductOptionAvailabilityItemResponse> options = group.options().stream()
            .map(this::toProductOptionAvailabilityItemResponse)
            .toList();

        return ProductOptionAvailabilityGroupResponse.from(
            group.optionGroupId(),
            group.optionType(),
            group.name(),
            group.required(),
            group.minSelect(),
            group.maxSelect(),
            group.linkedProductNames(),
            group.sort(),
            options
        );
    }

    private ProductOptionAvailabilityItemResponse toProductOptionAvailabilityItemResponse(
        ProductOptionAvailabilityItemResult option
    ) {
        return ProductOptionAvailabilityItemResponse.from(
            option.id(),
            option.optionType(),
            option.name(),
            option.additionalPrice(),
            option.soldOut(),
            option.soldOutUntil(),
            option.visible(),
            option.sort()
        );
    }

    /**
     * 카테고리 그룹핑 키 — {@code categoryId}가 null인 경우(카테고리 미지정)도 하나의 키로 다뤄야 하므로
     * record로 묶는다.
     */
    private record CategoryKey(Long categoryId, String categoryName, Integer categorySort) {
    }
}
