package com.tastyhouse.batch.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 품절 자동해제 배치의 건별 격리 단위 테스트.
 *
 * <p>{@code @Transactional} 프록시 없이 executor를 직접 조립하므로 트랜잭션 경계 자체는 검증 대상이
 * 아니다 — 이 테스트가 지키는 것은 <b>한 건 실패가 다음 건 처리를 멈추지 않는다</b>는 계약이다.
 */
class ProductSoldOutReleaseSchedulerServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final LocalDateTime PAST = LocalDateTime.of(2026, 8, 17, 9, 0);

    @Test
    @DisplayName("한 건이 실패해도 예외를 삼키고 나머지 건을 계속 처리한다")
    void releaseExpiredSoldOut_continuesAfterFailure() {
        Product failing = soldOutProduct(10L, "실패메뉴");
        Product healthy = soldOutProduct(11L, "정상메뉴");

        // id=10 저장 시에만 터지는 스텁 — 첫 건 실패가 두 번째 건을 막지 않아야 한다.
        ProductRepositoryStub productRepository =
            new ProductRepositoryStub(List.of(failing, healthy), 10L);
        Fixture fixture = new Fixture(productRepository,
            new ProductOptionRepositoryStub(List.of()), new ProductCommonOptionRepositoryStub(List.of()));

        assertThatCode(fixture.service::releaseExpiredSoldOut).doesNotThrowAnyException();

        // 실패한 건은 저장되지 않았고, 뒤의 건은 해제·저장됐다.
        assertThat(productRepository.saved).containsExactly(healthy);
        assertThat(healthy.isSoldOut()).isFalse();
        assertThat(healthy.getSoldOutUntil()).isNull();
    }

    @Test
    @DisplayName("메뉴·옵션·공통옵션 세 종류를 모두 해제한다")
    void releaseExpiredSoldOut_releasesAllThreeKinds() {
        Product product = soldOutProduct(10L, "떡볶이");
        ProductOption option = soldOutOption(100L, "곱빼기");
        ProductCommonOption commonOption = soldOutCommonOption(200L, "포크");

        Fixture fixture = new Fixture(
            new ProductRepositoryStub(List.of(product), null),
            new ProductOptionRepositoryStub(List.of(option)),
            new ProductCommonOptionRepositoryStub(List.of(commonOption)));

        fixture.service.releaseExpiredSoldOut();

        assertThat(product.isSoldOut()).isFalse();
        assertThat(product.getSoldOutUntil()).isNull();
        assertThat(option.isSoldOut()).isFalse();
        assertThat(option.getSoldOutUntil()).isNull();
        assertThat(commonOption.isSoldOut()).isFalse();
        assertThat(commonOption.getSoldOutUntil()).isNull();
    }

    @Test
    @DisplayName("대상이 없으면 아무것도 저장하지 않는다")
    void releaseExpiredSoldOut_noTargets_savesNothing() {
        ProductRepositoryStub productRepository = new ProductRepositoryStub(List.of(), null);
        Fixture fixture = new Fixture(productRepository,
            new ProductOptionRepositoryStub(List.of()), new ProductCommonOptionRepositoryStub(List.of()));

        fixture.service.releaseExpiredSoldOut();

        assertThat(productRepository.saved).isEmpty();
    }

    private static Product soldOutProduct(Long id, String name) {
        Product product = Product.reconstitute(
            id, SHOP_ID, ProductCategoryId.of(2L), name, "설명", 10000,
            null, null, 0, false, null, false, null, true, 1, false, null, null
        );
        product.markSoldOut(PAST);
        return product;
    }

    private static ProductOption soldOutOption(Long id, String name) {
        ProductOption option = ProductOption.reconstitute(
            id, ProductOptionGroupId.of(20L), name, 1000, 1, false, null, true);
        option.markSoldOut(PAST);
        return option;
    }

    private static ProductCommonOption soldOutCommonOption(Long id, String name) {
        ProductCommonOption option = ProductCommonOption.reconstitute(
            id, ProductOptionGroupId.of(30L), name, 0, 1, false, null, true);
        option.markSoldOut(PAST);
        return option;
    }

    /**
     * 스케줄러 서비스와 executor를 실제 조립대로 묶는 픽스처.
     */
    private static final class Fixture {

        private final ProductSoldOutReleaseSchedulerService service;

        private Fixture(
            ProductRepository productRepository,
            ProductOptionRepository productOptionRepository,
            ProductCommonOptionRepository productCommonOptionRepository
        ) {
            ProductSoldOutReleaseExecutor executor = new ProductSoldOutReleaseExecutor(
                productRepository, productOptionRepository, productCommonOptionRepository);
            this.service = new ProductSoldOutReleaseSchedulerService(
                productRepository, productOptionRepository, productCommonOptionRepository, executor);
        }
    }

    private static final class ProductRepositoryStub implements ProductRepository {

        private final List<Product> expired;
        private final Long failingId;
        private final List<Product> saved = new ArrayList<>();

        /**
         * @param failingId 이 id를 저장할 때만 예외를 던진다({@code null}이면 모두 성공)
         */
        private ProductRepositoryStub(List<Product> expired, Long failingId) {
            this.expired = expired;
            this.failingId = failingId;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.empty();
        }

        @Override
        public Product save(Product product) {
            if (failingId != null && failingId.equals(product.getId())) {
                throw new IllegalStateException("저장 실패 모사: productId=" + product.getId());
            }
            saved.add(product);
            return product;
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
            return expired;
        }
    }

    private record ProductOptionRepositoryStub(List<ProductOption> expired) implements ProductOptionRepository {

        @Override
        public Optional<ProductOption> findById(ProductOptionId id) {
            return Optional.empty();
        }

        @Override
        public ProductOption save(ProductOption productOption) {
            return productOption;
        }

        @Override
        public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
            return List.of();
        }

        @Override
        public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return List.of();
        }

        @Override
        public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return expired;
        }
    }

    private record ProductCommonOptionRepositoryStub(
        List<ProductCommonOption> expired
    ) implements ProductCommonOptionRepository {

        @Override
        public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
            return Optional.empty();
        }

        @Override
        public ProductCommonOption save(ProductCommonOption productCommonOption) {
            return productCommonOption;
        }

        @Override
        public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
            return List.of();
        }

        @Override
        public List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return List.of();
        }

        @Override
        public List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return expired;
        }
    }
}
