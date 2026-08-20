package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴그룹·메뉴 정렬과 그룹 이동의 순수 단위 테스트.
 *
 * <p>핵심 두 가지: <b>sort를 클라이언트에서 받지 않는다</b>(id 배열만 받아 0..N-1 정규화)와
 * <b>그룹 이동 시 출발 그룹도 재정규화한다</b>(빠져나간 자리에 구멍을 남기지 않는다).
 */
class ProductSortServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    // ── 메뉴그룹 순서 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴그룹 순서는 id 배열 순서대로 0..N-1로 정규화된다")
    void reorderCategories_normalizesToZeroBased() {
        Fixture fixture = new Fixture();
        fixture.addCategory(100L, 0);
        fixture.addCategory(101L, 1);
        fixture.addCategory(102L, 2);

        fixture.service.reorderCategories(SHOP_ID, List.of(
            ProductCategoryId.of(102L), ProductCategoryId.of(100L), ProductCategoryId.of(101L)));

        assertThat(fixture.category(102L).getSort()).isZero();
        assertThat(fixture.category(100L).getSort()).isEqualTo(1);
        assertThat(fixture.category(101L).getSort()).isEqualTo(2);
    }

    @Test
    @DisplayName("★ 요청 집합이 현재 집합과 다르면 거부한다 — 다른 탭에서 추가·삭제된 stale 요청 방어")
    void reorderCategories_setMismatch_rejected() {
        Fixture fixture = new Fixture();
        fixture.addCategory(100L, 0);
        fixture.addCategory(101L, 1);

        // 낡은 화면이 102번을 모른 채 두 개만 보냈다.
        assertThatThrownBy(() -> fixture.service.reorderCategories(SHOP_ID,
            List.of(ProductCategoryId.of(100L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);
    }

    // ── 그룹 내 메뉴 순서 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("그룹 내 메뉴 순서도 0..N-1로 정규화된다")
    void reorderProducts_normalizesToZeroBased() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);
        fixture.addProduct(11L, 100L, 1);

        fixture.service.reorderProducts(SHOP_ID, ProductCategoryId.of(100L),
            List.of(ProductId.of(11L), ProductId.of(10L)));

        assertThat(fixture.product(11L).getSort()).isZero();
        assertThat(fixture.product(10L).getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("★ 미분류 메뉴 목록(productCategoryId = null)도 재정렬 대상이다")
    void reorderProducts_uncategorized_isSupported() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, null, 0);
        fixture.addProduct(11L, null, 1);
        // 다른 그룹의 메뉴는 대상에 섞이지 않아야 한다.
        fixture.addProduct(12L, 100L, 0);

        fixture.service.reorderProducts(SHOP_ID, null, List.of(ProductId.of(11L), ProductId.of(10L)));

        assertThat(fixture.product(11L).getSort()).isZero();
        assertThat(fixture.product(10L).getSort()).isEqualTo(1);
        assertThat(fixture.product(12L).getSort()).isZero();
    }

    @Test
    @DisplayName("메뉴 순서 요청 집합이 현재와 다르면 거부한다")
    void reorderProducts_setMismatch_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);
        fixture.addProduct(11L, 100L, 1);

        assertThatThrownBy(() -> fixture.service.reorderProducts(SHOP_ID, ProductCategoryId.of(100L),
            List.of(ProductId.of(10L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
    }

    // ── 그룹 이동 ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("메뉴를 다른 그룹으로 옮기고 도착 그룹의 순서를 요청대로 정규화한다")
    void relocateProducts_movesAndOrdersTarget() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0); // 출발 그룹
        fixture.addProduct(20L, 200L, 0); // 도착 그룹 기존 메뉴

        // 10번을 200번 그룹의 맨 앞에 놓는다.
        fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(ProductId.of(10L)),
            List.of(ProductId.of(10L), ProductId.of(20L)));

        assertThat(fixture.product(10L).getProductCategoryId().value()).isEqualTo(200L);
        assertThat(fixture.product(10L).getSort()).isZero();
        assertThat(fixture.product(20L).getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("★ 출발 그룹의 sort도 0..N-1로 재정규화된다 — 빠져나간 자리에 구멍을 남기지 않는다")
    void relocateProducts_renumbersSourceGroup() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);
        fixture.addProduct(11L, 100L, 1);
        fixture.addProduct(12L, 100L, 2);

        // 가운데(11번)를 다른 그룹으로 옮긴다 → 출발 그룹에 sort 1 자리가 빈다.
        fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(ProductId.of(11L)),
            List.of(ProductId.of(11L)));

        assertThat(fixture.product(10L).getSort()).isZero();
        assertThat(fixture.product(12L).getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("미분류에서 그룹으로 옮기면 미분류 목록도 재정규화된다")
    void relocateProducts_renumbersUncategorizedSource() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, null, 0);
        fixture.addProduct(11L, null, 1);
        fixture.addProduct(12L, null, 2);

        fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(ProductId.of(11L)),
            List.of(ProductId.of(11L)));

        assertThat(fixture.product(10L).getSort()).isZero();
        assertThat(fixture.product(12L).getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("그룹에서 미분류로 옮길 수 있다(targetProductCategoryId = null)")
    void relocateProducts_toUncategorized() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);

        fixture.service.relocateProducts(SHOP_ID, null,
            List.of(ProductId.of(10L)),
            List.of(ProductId.of(10L)));

        assertThat(fixture.product(10L).getProductCategoryId()).isNull();
        assertThat(fixture.product(10L).getSort()).isZero();
    }

    @Test
    @DisplayName("이동 대상이 도착 순서 목록에 없으면 거부한다")
    void relocateProducts_movedNotInTargetOrder_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);
        fixture.addProduct(20L, 200L, 0);

        assertThatThrownBy(() -> fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(ProductId.of(10L)),
            List.of(ProductId.of(20L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
    }

    @Test
    @DisplayName("도착 순서 목록이 (도착 그룹 ∪ 이동 대상)과 다르면 거부한다")
    void relocateProducts_targetSetMismatch_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, 100L, 0);
        fixture.addProduct(20L, 200L, 0);
        fixture.addProduct(21L, 200L, 1);

        // 도착 그룹에 21번이 있는데 목록에서 빠졌다.
        assertThatThrownBy(() -> fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(ProductId.of(10L)),
            List.of(ProductId.of(10L), ProductId.of(20L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
    }

    @Test
    @DisplayName("이동 대상이 비어 있으면 요청 전체를 거절한다")
    void relocateProducts_emptyMoved_rejected() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.relocateProducts(SHOP_ID, ProductCategoryId.of(200L),
            List.of(), List.of()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
    }

    // ── 픽스처 ─────────────────────────────────────────────────────────────────────

    private static final class Fixture {

        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final Map<Long, ProductCategory> categories = new LinkedHashMap<>();
        private final ProductSortService service;

        private Fixture() {
            this.service = new ProductSortService(
                new StubProductRepository(products), new StubProductCategoryRepository(categories));
        }

        private void addProduct(Long id, Long categoryId, Integer sort) {
            products.put(id, Product.reconstitute(
                id, SHOP_ID, categoryId == null ? null : ProductCategoryId.of(categoryId),
                "메뉴" + id, null, 10000, null, null, 0,
                false, null, false, null, true, sort,
                false, false, null, false, null, null, null, null, null
            ));
        }

        private void addCategory(Long id, Integer sort) {
            categories.put(id, ProductCategory.reconstitute(id, SHOP_ID, "그룹" + id, null, sort, true));
        }

        private Product product(Long id) {
            return products.get(id);
        }

        private ProductCategory category(Long id) {
            return categories.get(id);
        }
    }

    private record StubProductRepository(Map<Long, Product> products) implements ProductRepository {

        @Override
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            Long target = productCategoryId == null ? null : productCategoryId.value();
            return products.values().stream()
                .filter(product -> {
                    Long actual = product.getProductCategoryId() == null
                        ? null : product.getProductCategoryId().value();
                    return java.util.Objects.equals(actual, target);
                })
                .sorted(Comparator.comparing(Product::getSort,
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            return ids.stream()
                .map(id -> products.get(id.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(products.get(id.value()));
        }

        @Override
        public Optional<Product> findByIdIncludingDeleted(ProductId id) {
            return findById(id);
        }

        @Override
        public long countVisibleByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            throw new UnsupportedOperationException();
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
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }

    private record StubProductCategoryRepository(Map<Long, ProductCategory> categories)
        implements ProductCategoryRepository {

        @Override
        public List<ProductCategory> findAllByShopId(ShopId shopId) {
            return categories.values().stream()
                .sorted(Comparator.comparing(ProductCategory::getSort,
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        }

        @Override
        public ProductCategory save(ProductCategory productCategory) {
            return productCategory;
        }

        @Override
        public Optional<ProductCategory> findById(ProductCategoryId id) {
            return Optional.ofNullable(categories.get(id.value()));
        }

        @Override
        public List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(ProductCategory productCategory) {
            categories.remove(productCategory.getId());
        }
    }
}
