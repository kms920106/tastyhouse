package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 상품 리뷰 통계 갱신 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트와 조회 포트를 손으로 만든 스텁으로 대체해
 * 검증한다(도메인 서비스 하강으로 얻는 테스트 용이성).
 */
class ProductReviewStatsServiceTest {

    private static final Long PRODUCT_ID = 7L;

    @Test
    @DisplayName("맛·양·가격 세 평점의 평균을 소수점 첫째 자리로 반영하고 리뷰 수를 갱신한다")
    void updateReviewStats_averagesThreeRatings() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(12L, 4.0, 5.0, 3.0);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getRating()).isEqualTo(4.0);
        assertThat(repository.saved.getFirst().getReviewCount()).isEqualTo(12);
    }

    @Test
    @DisplayName("일부 평점만 있으면 있는 항목만으로 평균을 낸다")
    void updateReviewStats_averagesOnlyPresentRatings() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(3L, 5.0, null, 4.0);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved.getFirst().getRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("세 평점이 모두 없으면 평점을 null로 둔다")
    void updateReviewStats_nullRatingWhenNoneAvailable() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(0L, null, null, null);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved.getFirst().getRating()).isNull();
        assertThat(repository.saved.getFirst().getReviewCount()).isZero();
    }

    @Test
    @DisplayName("리뷰 수가 null이면 0으로 반영한다")
    void updateReviewStats_treatsNullCountAsZero() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(null, 4.0, 4.0, 4.0);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved.getFirst().getReviewCount()).isZero();
    }

    @Test
    @DisplayName("상품이 없으면(이미 삭제됨) 조용히 넘어가고 저장하지 않는다")
    void updateReviewStats_skipsWhenProductMissing() {
        ProductRepositoryStub repository = new ProductRepositoryStub(null);
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(5L, 4.0, 4.0, 4.0);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved).isEmpty();
    }

    private Product product() {
        return Product.reconstitute(
            PRODUCT_ID,
            ShopId.of(1L),
            ProductCategoryId.of(2L),
            "황금올리브치킨",
            "바삭한 치킨",
            20000,
            null,
            null,
            0,
            true,
            1,
            false,
            true,
            0,
            null,
            null
        );
    }

    private static final class ProductRepositoryStub implements ProductRepository {

        private final Product product;
        private final List<Product> saved = new ArrayList<>();

        private ProductRepositoryStub(Product product) {
            this.product = product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(product);
        }

        @Override
        public Product save(Product product) {
            saved.add(product);
            return product;
        }
    }

    private record ProductReviewStatisticsPortStub(
        Long count,
        Double taste,
        Double amount,
        Double price
    ) implements ProductReviewStatisticsPort {

        @Override
        public Long countVisibleReviewsByProductId(Long productId) {
            return count;
        }

        @Override
        public Double getAverageTasteRatingByProductId(Long productId) {
            return taste;
        }

        @Override
        public Double getAverageAmountRatingByProductId(Long productId) {
            return amount;
        }

        @Override
        public Double getAveragePriceRatingByProductId(Long productId) {
            return price;
        }
    }
}
