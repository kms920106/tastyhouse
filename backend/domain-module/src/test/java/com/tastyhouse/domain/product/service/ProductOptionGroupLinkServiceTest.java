package com.tastyhouse.domain.product.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 ↔ 옵션그룹 연결의 <b>단일 가게 불변식</b> 순수 단위 테스트.
 *
 * <p>이 불변식이 깨지면 소유권 판정에서 ANY/ALL 구분이 필요해지고, 결국 "남의 가게 옵션을
 * 품절 처리할 수 있는가"라는 질문에 답이 없어진다.
 */
class ProductOptionGroupLinkServiceTest {

    private static final ShopId MY_SHOP = ShopId.of(1L);
    private static final ShopId OTHER_SHOP = ShopId.of(2L);

    @Test
    @DisplayName("★ 다른 가게 메뉴에 이미 연결된 옵션그룹은 연결할 수 없다 — 단일 가게 불변식")
    void link_otherShopGroup_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.addProduct(20L, OTHER_SHOP);
        // 그룹 100번은 이미 남의 가게 메뉴(20)에 연결돼 있다.
        fixture.links.seed(20L, 100L, 0);

        assertThatThrownBy(() ->
            fixture.service.link(ProductId.of(10L), ProductOptionGroupId.of(100L)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
    }

    @Test
    @DisplayName("같은 가게의 다른 메뉴에는 연결할 수 있다 — 이것이 N:M의 목적이다")
    void link_sameShopGroup_allowed() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.addProduct(11L, MY_SHOP);
        fixture.links.seed(11L, 100L, 0);

        fixture.service.link(ProductId.of(10L), ProductOptionGroupId.of(100L));

        assertThat(fixture.links.findAllByOptionGroupId(ProductOptionGroupId.of(100L))).hasSize(2);
    }

    @Test
    @DisplayName("연결이 0건인 새 그룹은 어느 메뉴에나 붙일 수 있다")
    void link_orphanGroup_allowed() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);

        fixture.service.link(ProductId.of(10L), ProductOptionGroupId.of(100L));

        assertThat(fixture.links.existsByProductIdAndOptionGroupId(
            ProductId.of(10L), ProductOptionGroupId.of(100L))).isTrue();
    }

    @Test
    @DisplayName("이미 연결돼 있으면 아무 일도 하지 않는다(멱등)")
    void link_alreadyLinked_isIdempotent() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);

        fixture.service.link(ProductId.of(10L), ProductOptionGroupId.of(100L));

        assertThat(fixture.links.findAllByOptionGroupId(ProductOptionGroupId.of(100L))).hasSize(1);
    }

    @Test
    @DisplayName("sort는 이 메뉴의 기존 연결 개수(맨 뒤)로 부여된다")
    void link_appendsToTail() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);
        fixture.links.seed(10L, 101L, 1);

        fixture.service.link(ProductId.of(10L), ProductOptionGroupId.of(102L));

        List<ProductOptionGroupLink> links = fixture.links.findAllByProductId(ProductId.of(10L));
        assertThat(links).extracting(ProductOptionGroupLink::getSort).containsExactly(0, 1, 2);
    }

    // ── 마지막 연결 해제 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("★ 마지막 연결은 해제할 수 없다 — 어디서도 보이지 않는 고아 그룹이 된다")
    void unlink_lastLink_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);

        assertThatThrownBy(() ->
            fixture.service.unlink(ProductId.of(10L), ProductOptionGroupId.of(100L)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK);
    }

    @Test
    @DisplayName("연결이 2건 이상이면 해제할 수 있고, 남은 연결의 sort가 0..N-1로 재정규화된다")
    void unlink_renumbersRemaining() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.addProduct(11L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);
        fixture.links.seed(10L, 101L, 1);
        fixture.links.seed(10L, 102L, 2);
        fixture.links.seed(11L, 100L, 0); // 그룹 100은 다른 메뉴에도 연결돼 있어 해제 가능

        fixture.service.unlink(ProductId.of(10L), ProductOptionGroupId.of(100L));

        List<ProductOptionGroupLink> remaining = fixture.links.findAllByProductId(ProductId.of(10L));
        assertThat(remaining).hasSize(2);
        assertThat(remaining).extracting(ProductOptionGroupLink::getSort).containsExactly(0, 1);
    }

    // ── 소유 가게 역조회 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("옵션그룹의 소유 가게를 그룹 → 링크 → 메뉴 → 가게로 역조회한다")
    void findOwningShopId_resolvesThroughLink() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);

        assertThat(fixture.service.findOwningShopId(ProductOptionGroupId.of(100L))).isEqualTo(MY_SHOP);
    }

    @Test
    @DisplayName("연결이 0건인 그룹은 소유자가 없다(null) — 호출부는 이를 접근 불가로 다뤄야 한다")
    void findOwningShopId_orphanGroup_isNull() {
        Fixture fixture = new Fixture();

        assertThat(fixture.service.findOwningShopId(ProductOptionGroupId.of(999L))).isNull();
    }

    // ── 순서 변경 ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("순서는 id 배열로 받아 0..N-1로 정규화한다 — sort 값을 받지 않는다")
    void reorder_normalizesToZeroBased() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);
        fixture.links.seed(10L, 101L, 1);
        fixture.links.seed(10L, 102L, 2);

        fixture.service.reorder(ProductId.of(10L), List.of(
            ProductOptionGroupId.of(102L),
            ProductOptionGroupId.of(100L),
            ProductOptionGroupId.of(101L)
        ));

        assertThat(fixture.links.findAllByProductId(ProductId.of(10L)))
            .extracting(link -> link.getOptionGroupId().value())
            .containsExactly(102L, 100L, 101L);
    }

    @Test
    @DisplayName("요청 집합이 현재 연결 집합과 다르면 거부한다 — 다른 탭의 stale 요청 방어")
    void reorder_setMismatch_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.links.seed(10L, 100L, 0);
        fixture.links.seed(10L, 101L, 1);

        assertThatThrownBy(() -> fixture.service.reorder(ProductId.of(10L),
            List.of(ProductOptionGroupId.of(100L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
    }

    // ── 픽스처 ─────────────────────────────────────────────────────────────────────

    private static final class Fixture {

        private final FakeProductOptionGroupLinkRepository links = new FakeProductOptionGroupLinkRepository();
        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final ProductOptionGroupLinkService service;

        private Fixture() {
            this.service = new ProductOptionGroupLinkService(links, new StubProductRepository(products));
        }

        private void addProduct(Long id, ShopId shopId) {
            products.put(id, Product.reconstitute(
                id, shopId, null, "메뉴" + id, null, 1000, null, null, 0,
                false, null, false, null, true, 0,
                false, false, null, false, null, null, null, null, null
            ));
        }
    }

    /** {@code findById}만 쓰는 최소 스텁. 나머지는 이 테스트가 호출하지 않는다. */
    private static final class StubProductRepository implements ProductRepository {

        private final Map<Long, Product> products;

        private StubProductRepository(Map<Long, Product> products) {
            this.products = products;
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
        public Product save(Product product) {
            return product;
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            throw new UnsupportedOperationException();
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
        public List<Product> findAllSoldOutExpiredBefore(java.time.LocalDateTime baseTime) {
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
        public List<Product> findAllByShopIdAndCategoryId(
            ShopId shopId,
            com.tastyhouse.domain.product.vo.ProductCategoryId productCategoryId
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(com.tastyhouse.domain.product.vo.ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }
}
