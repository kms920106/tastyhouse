package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductDeletionServiceTest {
    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Test
    @DisplayName("★ 마지막 노출 메뉴는 삭제할 수 없다 — 숨김과 같은 제약이 걸린다")
    void deleteProducts_lastVisible_rejected() {
        Product only = product(10L, "떡볶이", true, false, 0);
        Fixture fixture = Fixture.of(List.of(only), 1, 0);

        ProductAvailabilityChangeResult result =
            fixture.service.deleteProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.succeeded()).isEmpty();
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        assertThat(only.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("마지막 추천 메뉴는 삭제할 수 없다")
    void deleteProducts_lastRepresentative_rejected() {
        Product representative = product(10L, "대표메뉴", true, true, 0);
        Fixture fixture = Fixture.of(List.of(representative), 3, 1);

        ProductAvailabilityChangeResult result =
            fixture.service.deleteProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        assertThat(representative.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("제약에 걸리면 sort 뒤쪽부터 되돌린다 — 앞선(노출 우선) 메뉴를 남긴다")
    void deleteProducts_rejectsFromTail() {
        Product first = product(10L, "1번", true, false, 0);
        Product second = product(11L, "2번", true, false, 1);
        Fixture fixture = Fixture.of(List.of(first, second), 2, 0);

        ProductAvailabilityChangeResult result = fixture.service.deleteProducts(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)));

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(result.failed()).extracting(ProductAvailabilityFailure::id).containsExactly(11L);
        assertThat(first.isDeleted()).isTrue();
        assertThat(second.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("추천 메뉴를 되돌리면 노출 부족분도 함께 해소된다 — 판정과 되돌리기는 각각 한 번이다")
    void deleteProducts_representativeRollbackAlsoSatisfiesVisible() {
        Product normal = product(10L, "일반", true, false, 0);
        Product representative = product(11L, "추천", true, true, 1);
        Fixture fixture = Fixture.of(List.of(normal, representative), 2, 1);

        ProductAvailabilityChangeResult result = fixture.service.deleteProducts(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)));

        assertThat(result.failed()).hasSize(1);
        assertThat(result.succeeded()).containsExactly(10L);
    }

    @Test
    @DisplayName("부분실패 판정은 요청 순서와 무관하다")
    void deleteProducts_orderIndependent() {
        Product first = product(10L, "1번", true, false, 0);
        Product second = product(11L, "2번", true, false, 1);
        Fixture forward = Fixture.of(List.of(first, second), 2, 0);
        ProductAvailabilityChangeResult forwardResult = forward.service.deleteProducts(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)));

        Product first2 = product(10L, "1번", true, false, 0);
        Product second2 = product(11L, "2번", true, false, 1);
        Fixture reversed = Fixture.of(List.of(first2, second2), 2, 0);
        ProductAvailabilityChangeResult reversedResult = reversed.service.deleteProducts(
            SHOP_ID, List.of(ProductId.of(11L), ProductId.of(10L)));

        assertThat(forwardResult.succeeded()).isEqualTo(reversedResult.succeeded());
        assertThat(forwardResult.failed()).extracting(ProductAvailabilityFailure::id)
            .isEqualTo(reversedResult.failed().stream().map(ProductAvailabilityFailure::id).toList());
    }

    @Test
    @DisplayName("★ 삭제는 visible도 함께 끈다 — deleted 필터를 빠뜨린 읽기경로에 대한 두 겹 방어")
    void deleteProducts_alsoTurnsOffVisible() {
        Product target = product(10L, "지울메뉴", true, false, 1);
        Fixture fixture = Fixture.of(List.of(target), 5, 2);

        fixture.service.deleteProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(target.isDeleted()).isTrue();
        assertThat(target.isVisible()).isFalse();
    }

    @Test
    @DisplayName("이미 삭제된 메뉴를 다시 삭제해도 실패가 아니다(멱등)")
    void deleteProducts_alreadyDeleted_isIdempotent() {
        Product target = product(10L, "이미삭제", true, false, 1);
        target.delete();
        Fixture fixture = Fixture.of(List.of(target), 5, 2);

        ProductAvailabilityChangeResult result =
            fixture.service.deleteProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(result.failed()).isEmpty();
    }

    @Test
    @DisplayName("이미 숨김인 메뉴는 노출 제약 계산에서 제외되지만 삭제는 그대로 수행된다")
    void deleteProducts_alreadyHidden_excludedFromConstraint() {
        Product hidden = product(10L, "숨긴메뉴", false, false, 1);
        Fixture fixture = Fixture.of(List.of(hidden), 1, 1);

        ProductAvailabilityChangeResult result =
            fixture.service.deleteProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(hidden.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("대상이 비어 있으면 부분실패가 아니라 요청 전체를 거절한다")
    void deleteProducts_emptyTarget_rejectsWholeRequest() {
        Fixture fixture = Fixture.of(List.of(), 5, 2);

        assertThatThrownBy(() -> fixture.service.deleteProducts(SHOP_ID, List.of()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
    }

    @Test
    @DisplayName("없는 메뉴·남의 가게 메뉴는 같은 코드로 부분실패에 담긴다 — 존재 여부를 알려주지 않는다")
    void deleteProducts_notFound_isPartialFailure() {
        Fixture fixture = Fixture.of(List.of(product(10L, "내메뉴", true, false, 0)), 5, 2);

        ProductAvailabilityChangeResult result = fixture.service.deleteProducts(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(999L)));

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(result.failed().getFirst().id()).isEqualTo(999L);
    }

    private static Product product(Long id, String name, boolean visible, boolean representative, Integer sort) {
        Product product = Product.reconstitute(
            id, SHOP_ID, ProductCategoryId.of(2L), name, null, 10000, null, null, 0,
            representative, null, false, null, true, sort,
            false, false, null, false, null, null, null, null, null, null
        );
        if (!visible) {
            product.deactivate();
        }
        return product;
    }

    private record Fixture(ProductDeletionService service) {
        private static Fixture of(List<Product> products, long visibleCount, long representativeCount) {
            return new Fixture(new ProductDeletionService(
                new StubProductRepository(products, visibleCount, representativeCount)));
        }
    }

    private static final class StubProductRepository implements ProductRepository {
        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final long visibleCount;
        private final long representativeCount;

        private StubProductRepository(List<Product> products, long visibleCount, long representativeCount) {
            products.forEach(product -> this.products.put(product.getId(), product));
            this.visibleCount = visibleCount;
            this.representativeCount = representativeCount;
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            return ids.stream()
                .map(id -> products.get(id.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }

        @Override
        public long countVisibleByShopId(ShopId shopId) {
            return visibleCount;
        }

        @Override
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            return representativeCount;
        }

        @Override
        public long countRepresentativeByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
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
            return Optional.ofNullable(products.get(id.value()));
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
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }
}
