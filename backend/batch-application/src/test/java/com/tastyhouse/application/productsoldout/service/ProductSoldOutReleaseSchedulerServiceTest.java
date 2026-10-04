package com.tastyhouse.application.productsoldout.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProductSoldOutReleaseSchedulerServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final LocalDateTime PAST = LocalDateTime.of(2026, 8, 17, 9, 0);

    @Test
    @DisplayName("한 건이 실패해도 예외를 삼키고 나머지 건을 계속 처리한다")
    void releaseExpiredSoldOut_continuesAfterFailure() {
        Product failing = soldOutProduct(10L, "실패메뉴");
        Product healthy = soldOutProduct(11L, "정상메뉴");

        ProductPersistencePortStub productPersistencePort =
            new ProductPersistencePortStub(List.of(failing, healthy), 10L);
        Fixture fixture = new Fixture(productPersistencePort,
            new ProductOptionPersistencePortStub(List.of()), new ProductCommonOptionPersistencePortStub(List.of()));

        assertThatCode(fixture.service::releaseExpiredSoldOut).doesNotThrowAnyException();

        assertThat(productPersistencePort.saved).containsExactly(healthy);
        assertThat(healthy.isSoldOut()).isFalse();
        assertThat(healthy.getSoldOutUntil()).isNull();
    }

    @Test
    @DisplayName("메뉴·옵션·공통옵션 세 종류를 모두 해제한다")
    void releaseExpiredSoldOut_releasesAllThreeKinds() {
        Product product = soldOutProduct(10L, "떡볶이");
        ProductOption option = soldOutOption();
        ProductCommonOption commonOption = soldOutCommonOption();

        Fixture fixture = new Fixture(
            new ProductPersistencePortStub(List.of(product), null),
            new ProductOptionPersistencePortStub(List.of(option)),
            new ProductCommonOptionPersistencePortStub(List.of(commonOption)));

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
        ProductPersistencePortStub productPersistencePort = new ProductPersistencePortStub(List.of(), null);
        Fixture fixture = new Fixture(productPersistencePort,
            new ProductOptionPersistencePortStub(List.of()), new ProductCommonOptionPersistencePortStub(List.of()));

        fixture.service.releaseExpiredSoldOut();

        assertThat(productPersistencePort.saved).isEmpty();
    }

    private static Product soldOutProduct(Long id, String name) {
        Product product = Product.reconstitute(
            id, SHOP_ID, ProductCategoryId.of(2L), name, "설명", 10000,
            null, null, 0, false, null, false, null, true, 1, false,
            false, null, false, null, null, null,
            null,
            null, null
        );
        product.markSoldOut(PAST);
        return product;
    }

    private static ProductOption soldOutOption() {
        ProductOption option = ProductOption.reconstitute(
            100L, ProductOptionGroupId.of(20L), "곱빼기", 1000, 1, false, null, true,
            null,
            null
        );
        option.markSoldOut(PAST);
        return option;
    }

    private static ProductCommonOption soldOutCommonOption() {
        ProductCommonOption option = ProductCommonOption.reconstitute(
            200L, ProductOptionGroupId.of(30L), "포크", 0, 1, false, null, true);
        option.markSoldOut(PAST);
        return option;
    }

    private static final class Fixture {

        private final ProductSoldOutReleaseSchedulerService service;

        private Fixture(
            ProductPersistencePort productPersistencePort,
            ProductOptionPersistencePort productOptionPersistencePort,
            ProductCommonOptionPersistencePort productCommonOptionPersistencePort
        ) {
            ProductSoldOutReleaseExecutor executor = new ProductSoldOutReleaseExecutor(
                productPersistencePort, productOptionPersistencePort, productCommonOptionPersistencePort);
            this.service = new ProductSoldOutReleaseSchedulerService(
                productPersistencePort, productOptionPersistencePort, productCommonOptionPersistencePort, executor);
        }
    }

    private static final class ProductPersistencePortStub implements ProductPersistencePort {

        private final List<Product> expired;
        private final Long failingId;
        private final List<Product> saved = new ArrayList<>();

        private ProductPersistencePortStub(List<Product> expired, Long failingId) {
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
        public long countRepresentativeByShopId(ShopId shopId) {

            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return expired;
        }

        @Override
        public Optional<Product> findByIdIncludingDeleted(ProductId id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByShopIdAndName(ShopId shopId, String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }

    private record ProductOptionPersistencePortStub(List<ProductOption> expired) implements ProductOptionPersistencePort {

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

    private record ProductCommonOptionPersistencePortStub(
        List<ProductCommonOption> expired
    ) implements ProductCommonOptionPersistencePort {

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
