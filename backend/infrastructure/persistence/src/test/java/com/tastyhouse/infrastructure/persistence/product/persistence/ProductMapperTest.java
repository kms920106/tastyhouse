package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    @Test
    @DisplayName("Product → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        Product product = sampleProduct();

        ProductJpaEntity entity = ProductMapper.toEntity(product);

        assertThat(entity.getShopId()).isEqualTo(222L);
        assertThat(entity.getProductCategoryId()).isEqualTo(223L);
        assertThat(entity.getName()).isEqualTo("김치찌개");
        assertThat(entity.getDescription()).isEqualTo("맛있는 찌개");
        assertThat(entity.getOriginalPrice()).isEqualTo(9000);
        assertThat(entity.getDiscountInfo().discountPrice()).isEqualTo(8000);
        assertThat(entity.getDiscountInfo().discountRate()).isEqualTo(new BigDecimal("11.11"));
        assertThat(entity.getRating()).isEqualTo(4.5);
        assertThat(entity.getReviewCount()).isEqualTo(12);
        assertThat(entity.isRepresentative()).isEqualTo(true);
        assertThat(entity.getSpiciness()).isEqualTo(3);
        assertThat(entity.isSoldOut()).isEqualTo(false);
        assertThat(entity.getSoldOutUntil()).isEqualTo(LocalDateTime.of(2026, 2, 21, 10, 21));
        assertThat(entity.isVisible()).isEqualTo(true);
        assertThat(entity.getSort()).isEqualTo(13);
        assertThat(entity.isRatingExcluded()).isEqualTo(false);
        assertThat(entity.isDeleted()).isEqualTo(true);
        assertThat(entity.getComposition()).isEqualTo("돼지고기, 김치");
        assertThat(entity.isSingleServing()).isEqualTo(false);
        assertThat(entity.getExposureStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(entity.getExposureEndDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(entity.getVegetarianType()).isEqualTo("PESCO");
        assertThat(entity.getWeightText()).isEqualTo("350g");
    }

    @Test
    @DisplayName("엔티티 → Product 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductJpaEntity entity = ProductJpaEntity.create(
            222L,
            223L,
            "김치찌개",
            "맛있는 찌개",
            9000,
            new ProductDiscountInfoEmbeddable(8000, new BigDecimal("11.11")),
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
            "PESCO",
            "350g"
        );
        ReflectionTestUtils.setField(entity, "id", 221L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 22, 10, 22));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 23, 10, 23));

        Product restored = ProductMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(sampleProduct());
    }

    private static Product sampleProduct() {
        return Product.reconstitute(
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
    }
}
