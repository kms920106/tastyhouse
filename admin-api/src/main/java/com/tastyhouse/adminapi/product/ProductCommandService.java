package com.tastyhouse.adminapi.product;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.domain.model.Product;
import com.tastyhouse.domain.product.domain.model.ProductCategory;
import com.tastyhouse.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.domain.product.domain.service.ProductRegistrationService;
import com.tastyhouse.domain.product.domain.vo.ProductId;

/**
 * 관리자 상품 command 서비스. 트랜잭션 경계를 소유하고, 불변식·저장은 도메인 서비스
 * {@link ProductRegistrationService}에 위임한다. 조회는 {@link ProductQueryService}가 담당한다.
 *
 * <p>HTTP 경계에서 받은 {@code Long} 식별자는 이 계층에서 {@code ProductId}로 승격한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRegistrationService productRegistrationService;

    public Long createProduct(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        Product product = productRegistrationService.createProduct(
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            representative,
            spiciness,
            soldOut,
            visible,
            sort
        );
        return product.getId();
    }

    public void updateProduct(
        Long id,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        ProductId productId = ProductId.of(id);
        productRegistrationService.updateProduct(
            productId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            representative,
            spiciness,
            soldOut,
            visible,
            sort
        );
    }

    public void markSoldOut(Long id) {
        ProductId productId = ProductId.of(id);
        productRegistrationService.markSoldOut(productId);
    }

    public void deactivateProduct(Long id) {
        ProductId productId = ProductId.of(id);
        productRegistrationService.deactivateProduct(productId);
    }

    public Long createProductOptionGroup(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        ProductOptionGroup optionGroup = productRegistrationService.saveProductOptionGroup(
            id,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            sort,
            visible
        );
        return optionGroup.getId();
    }

    public Long createProductOption(
        Long groupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        return productRegistrationService.saveProductOption(groupId, name, additionalPrice, sort, soldOut, visible);
    }

    public Long createProductImage(Long id, Long imageFileId, Integer sort, boolean visible) {
        return productRegistrationService.saveProductImage(id, imageFileId, sort, visible);
    }

    public Long createProductCategory(Long shopId, String name, Integer sort, boolean visible) {
        ProductCategory category = productRegistrationService.createProductCategory(shopId, name, sort, visible);
        return category.getId();
    }
}
