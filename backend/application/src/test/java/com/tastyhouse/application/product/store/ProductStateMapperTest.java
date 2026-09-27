package com.tastyhouse.application.product.store;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStateMapperTest {

    @Test
    @DisplayName("Product → ProductState → Product 왕복 시 모든 필드가 보존된다")
    void productRoundTrip() {
        Product original = Product.reconstitute(
            221L,
            ShopId.of(222L),
            ProductCategoryId.of(223L),
            "김치찌개",
            "맛있는 찌개",
            9000,
            ProductDiscountInfo.of(8000, new BigDecimal("11.11")),
            4.5,
            12,
            true,
            3,
            false,
            LocalDateTime.of(2026, 2, 21, 10, 21),
            true,
            13,
            false,
            true,
            "돼지고기, 김치",
            false,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 4, 1),
            VegetarianType.PESCO,
            "350g",
            LocalDateTime.of(2026, 2, 22, 10, 22),
            LocalDateTime.of(2026, 2, 23, 10, 23)
        );

        Product restored = ProductStateMapper.toDomain(ProductStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
