package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
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
 * 상품 메뉴 평가 통계 갱신 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트와 조회 포트를 손으로 만든 스텁으로 대체해
 * 검증한다(도메인 서비스 하강으로 얻는 테스트 용이성).
 *
 * <p>과거의 "맛·양·가격 3항목 부분 평균" 케이스는 사라졌다 — {@code PRODUCT.rating}의 근거가
 * MENU_REVIEW로 이관되며 평균 축이 {@code rating} 하나로 줄었기 때문이다.
 */
class ProductReviewStatsServiceTest {

    private static final Long PRODUCT_ID = 7L;

    @Test
    @DisplayName("메뉴 평가 평균을 소수점 첫째 자리로 반올림해 반영하고 평가 수를 갱신한다")
    void updateReviewStats_roundsAverageToTenth() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(12L, 4.266666);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.saved.getFirst().getRating()).isEqualTo(4.3);
        assertThat(repository.saved.getFirst().getReviewCount()).isEqualTo(12);
    }

    @Test
    @DisplayName("메뉴 평가가 없으면 평점을 null로 둔다(0.0이 아니다 — '평점 0점'과 구분)")
    void updateReviewStats_nullRatingWhenNoMenuReview() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(0L, null);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved.getFirst().getRating()).isNull();
        assertThat(repository.saved.getFirst().getReviewCount()).isZero();
    }

    @Test
    @DisplayName("평가 수가 null이면 0으로 반영한다")
    void updateReviewStats_treatsNullCountAsZero() {
        ProductRepositoryStub repository = new ProductRepositoryStub(product());
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(null, 4.0);
        ProductReviewStatsService service = new ProductReviewStatsService(repository, port);

        service.updateReviewStats(PRODUCT_ID);

        assertThat(repository.saved.getFirst().getReviewCount()).isZero();
    }

    @Test
    @DisplayName("상품이 없으면(이미 삭제됨) 조용히 넘어가고 저장하지 않는다")
    void updateReviewStats_skipsWhenProductMissing() {
        ProductRepositoryStub repository = new ProductRepositoryStub(null);
        ProductReviewStatisticsPortStub port = new ProductReviewStatisticsPortStub(5L, 4.0);
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
            null,
            true,
            0,
            false,
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
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            return List.of();
        }

        @Override
        public long countVisibleByShopId(ShopId shopId) {
            return 0L;
        }

        @Override
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            return 0L;
        }

        @Override
        public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return List.of();
        }

        @Override
        public Product save(Product product) {
            saved.add(product);
            return product;
        }
    }

    private record ProductReviewStatisticsPortStub(
        Long count,
        Double averageRating
    ) implements ProductReviewStatisticsPort {

        @Override
        public Long countVisibleMenuReviewsByProductId(Long productId) {
            return count;
        }

        @Override
        public Double getAverageMenuRatingByProductId(Long productId) {
            return averageRating;
        }
    }
}
