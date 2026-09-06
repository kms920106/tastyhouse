package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 가격 전체 교체(PUT)의 순수 단위 테스트.
 *
 * <p><b>{@code PRODUCT.original_price} 동기화가 이 테스트의 핵심이다.</b> 그 동기화가 빠지면 기존 주문
 * 경로가 옛 정가를 읽어 금액 대조에 실패하고 <b>주문이 전부 거절</b>된다 — 가격 행이 1개인 메뉴의
 * 동작이 그대로 유지되는 것이 이 설계의 안전장치이므로 그 지점을 못 박는다.
 */
class ProductPriceServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final ProductId PRODUCT_ID = ProductId.of(10L);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 12, 0);

    @Nested
    @DisplayName("original_price 동기화")
    class OriginalPriceSync {

        @Test
        @DisplayName("sort=0 행의 배달가가 PRODUCT.original_price에 반영된다")
        void syncsBasePriceToProduct() {
            Fixture fixture = new Fixture();

            fixture.replace(List.of(spec(null, null, 9000, null, null, 0)));

            assertThat(fixture.product().getOriginalPrice()).isEqualTo(9000);
        }

        @Test
        @DisplayName("가격이 여러 개면 sort가 가장 작은 행의 배달가가 반영된다")
        void syncsLowestSortRow() {
            Fixture fixture = new Fixture();

            fixture.replace(List.of(
                spec(null, "곱빼기", 12000, null, null, 1),
                spec(null, "보통", 9000, null, null, 0)
            ));

            assertThat(fixture.product().getOriginalPrice()).isEqualTo(9000);
        }
    }

    @Nested
    @DisplayName("컬렉션 불변식")
    class CollectionInvariants {

        @Test
        @DisplayName("빈 목록은 PRODUCT_PRICE_EMPTY로 거절된다")
        void emptyList_isRejected() {
            assertThatThrownBy(() -> new Fixture().replace(List.of()))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_EMPTY));
        }

        @Test
        @DisplayName("가격이 1개면 가격명이 없어도 된다")
        void singlePrice_allowsNullName() {
            assertThatCode(() -> new Fixture().replace(List.of(spec(null, null, 9000, null, null, 0))))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("가격이 2개 이상이면 가격명이 필수다")
        void multiplePrices_requireName() {
            assertThatThrownBy(() -> new Fixture().replace(List.of(
                spec(null, "보통", 9000, null, null, 0),
                spec(null, null, 12000, null, null, 1)
            )))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_NAME_REQUIRED));
        }

        @Test
        @DisplayName("가격명이 중복이면 거절된다 — DB 유니크 제약이 500으로 새지 않게 한다")
        void duplicateName_isRejected() {
            assertThatThrownBy(() -> new Fixture().replace(List.of(
                spec(null, "보통", 9000, null, null, 0),
                spec(null, "보통", 12000, null, null, 1)
            )))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_NAME_DUPLICATED));
        }
    }

    @Nested
    @DisplayName("인증 게이트")
    class VerificationGate {

        @Test
        @DisplayName("미인증 가게는 매장가를 설정할 수 없다")
        void unverifiedShop_cannotSetStorePrice() {
            Fixture fixture = new Fixture();

            assertThatThrownBy(() -> fixture.replace(List.of(spec(null, null, 9000, 9000, null, 0))))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_STORE_NOT_VERIFIED));
        }

        @Test
        @DisplayName("미인증 가게는 픽업가도 설정할 수 없다")
        void unverifiedShop_cannotSetPickupPrice() {
            Fixture fixture = new Fixture();

            assertThatThrownBy(() -> fixture.replace(List.of(spec(null, null, 9000, null, 8000, 0))))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_STORE_NOT_VERIFIED));
        }

        @Test
        @DisplayName("미인증 가게도 배달가는 자유롭게 바꿀 수 있다")
        void unverifiedShop_canChangeDeliveryPrice() {
            assertThatCode(() -> new Fixture().replace(List.of(spec(null, null, 9000, null, null, 0))))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("인증된 가게는 매장가·픽업가를 설정할 수 있다")
        void verifiedShop_canSetStoreAndPickupPrice() {
            Fixture fixture = new Fixture();
            fixture.verificationPort.verified = true;

            assertThatCode(() -> fixture.replace(List.of(spec(null, null, 8000, 9000, 8000, 0))))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("할인 진행 중 차단")
    class DiscountGate {

        @Test
        @DisplayName("할인가가 설정된 메뉴는 가격을 바꿀 수 없다")
        void discountedProduct_cannotChangePrice() {
            Fixture fixture = new Fixture(3000);

            assertThatThrownBy(() -> fixture.replace(List.of(spec(null, null, 9000, null, null, 0))))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_DISCOUNT_IN_PROGRESS));
        }
    }

    @Nested
    @DisplayName("재인증 필요 판정(인증 OFF)")
    class VerificationRefresh {

        @Test
        @DisplayName("배달가가 매장가를 넘으면 가게 인증이 즉시 내려간다")
        void deliveryAboveStore_clearsVerification() {
            Fixture fixture = new Fixture();
            fixture.verificationPort.verified = true;
            // 이미 저장된 다른 메뉴의 가격 행이 위반 상태가 된다.
            fixture.prices.seed(ProductPrice.reconstitute(
                900L, ProductId.of(99L), null, 12000, 9000, null, 0, null, null, null));

            fixture.replace(List.of(spec(null, null, 8000, 9000, null, 0)));

            assertThat(fixture.verificationPort.verified).isFalse();
        }

        @Test
        @DisplayName("모든 메뉴가 배달가 <= 매장가면 인증이 유지된다")
        void allWithinStorePrice_keepsVerification() {
            Fixture fixture = new Fixture();
            fixture.verificationPort.verified = true;

            fixture.replace(List.of(spec(null, null, 8000, 9000, null, 0)));

            assertThat(fixture.verificationPort.verified).isTrue();
        }

        @Test
        @DisplayName("위반 행의 매장가·픽업가·픽업가 설정시각이 함께 지워진다")
        void deliveryAboveStore_clearsStoreAndPickupPriceOfViolatingRow() {
            Fixture fixture = new Fixture();
            fixture.verificationPort.verified = true;
            ProductPrice violating = ProductPrice.reconstitute(
                900L, ProductId.of(99L), null, 12000, 9000, 9000, 0, NOW.minusDays(2), null, null);
            fixture.prices.seed(violating);

            fixture.replace(List.of(spec(null, null, 8000, 9000, null, 0)));

            ProductPrice stored = fixture.prices.rows.get(900L);
            assertThat(stored.getStorePrice()).isNull();
            assertThat(stored.getPickupPrice()).isNull();
            assertThat(stored.getPickupPriceSetAt()).isNull();
            // 배달가는 결제 가격이므로 인증이 풀려도 남는다.
            assertThat(stored.getDeliveryPrice()).isEqualTo(12000);
        }

        @Test
        @DisplayName("위반하지 않은 행의 매장가·픽업가는 보존된다")
        void nonViolatingRows_keepStoreAndPickupPrice() {
            Fixture fixture = new Fixture();
            fixture.verificationPort.verified = true;
            fixture.prices.seed(ProductPrice.reconstitute(
                900L, ProductId.of(99L), null, 12000, 9000, 9000, 0, NOW.minusDays(2), null, null));
            fixture.prices.seed(ProductPrice.reconstitute(
                901L, ProductId.of(98L), null, 7000, 9000, 8000, 0, NOW.minusDays(2), null, null));

            fixture.replace(List.of(spec(null, null, 8000, 9000, null, 0)));

            ProductPrice intact = fixture.prices.rows.get(901L);
            assertThat(intact.getStorePrice()).isEqualTo(9000);
            assertThat(intact.getPickupPrice()).isEqualTo(8000);
        }

        @Test
        @DisplayName("인증이 꺼진 가게는 자동으로 켜지지 않는다 — 켜는 것은 관리자 승인의 권한이다")
        void unverifiedShop_isNeverAutoVerified() {
            Fixture fixture = new Fixture();

            fixture.replace(List.of(spec(null, null, 8000, null, null, 0)));

            assertThat(fixture.verificationPort.verified).isFalse();
        }
    }

    @Nested
    @DisplayName("전체 교체 의미론")
    class ReplaceSemantics {

        @Test
        @DisplayName("요청에 담기지 않은 기존 행은 삭제된다")
        void omittedRowsAreDeleted() {
            Fixture fixture = new Fixture();
            fixture.prices.seed(ProductPrice.reconstitute(
                500L, PRODUCT_ID, "보통", 9000, null, null, 0, null, null, null));
            fixture.prices.seed(ProductPrice.reconstitute(
                501L, PRODUCT_ID, "곱빼기", 12000, null, null, 1, null, null, null));

            // 501만 남기고 보낸다 → 500은 삭제돼야 한다.
            fixture.replace(List.of(spec(501L, "곱빼기", 12000, null, null, 0)));

            assertThat(fixture.prices.deleted).containsExactly(500L);
        }

        @Test
        @DisplayName("다른 메뉴의 가격 행 id를 갱신 대상으로 보내면 거절된다")
        void foreignPriceId_isRejected() {
            Fixture fixture = new Fixture();
            fixture.prices.seed(ProductPrice.reconstitute(
                900L, ProductId.of(99L), null, 9000, null, null, 0, null, null, null));

            assertThatThrownBy(() -> fixture.replace(List.of(spec(900L, null, 9000, null, null, 0))))
                .satisfies(thrown -> assertThat(thrown.getMessage()).contains("가격 정보"));
        }
    }

    private static ProductPriceSpec spec(
        Long id,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort
    ) {
        return ProductPriceSpec.of(id, priceName, deliveryPrice, storePrice, pickupPrice, sort);
    }

    /** 가격 서비스와 그 협력자 스텁 한 벌. */
    private static final class Fixture {

        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final MapProductPriceRepository prices = new MapProductPriceRepository();
        private final RecordingVerificationPort verificationPort = new RecordingVerificationPort();
        private final ProductPriceService service;

        private Fixture() {
            this(null);
        }

        private Fixture(Integer discountPrice) {
            products.put(PRODUCT_ID.value(), Product.reconstitute(
                PRODUCT_ID.value(), SHOP_ID, ProductCategoryId.of(1L), "메뉴", null,
                1000,
                discountPrice == null ? null : com.tastyhouse.domain.product.vo.ProductDiscountInfo
                    .of(discountPrice, null),
                null, 0, false, null, false, null, true, 0,
                false, false, null, false, null, null, null, null, null, null
            ));
            this.service = new ProductPriceService(
                prices, new OwnedProductRepository(products), verificationPort);
        }

        private void replace(List<ProductPriceSpec> specs) {
            service.replacePrices(SHOP_ID, PRODUCT_ID, specs, NOW);
        }

        private Product product() {
            return products.get(PRODUCT_ID.value());
        }
    }

    /** 소유권 조회({@code findAllByShopIdAndIdIn})와 저장만 동작하는 스텁. */
    private static final class OwnedProductRepository implements ProductRepository {

        private final Map<Long, Product> products;

        private OwnedProductRepository(Map<Long, Product> products) {
            this.products = products;
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            List<Product> found = new ArrayList<>();
            for (ProductId id : ids) {
                Product product = products.get(id.value());
                if (product != null && product.getShopId().equals(shopId)) {
                    found.add(product);
                }
            }
            return found;
        }

        @Override
        public Product save(Product product) {
            products.put(product.getId(), product);
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
        public long countRepresentativeByShopId(ShopId shopId) {
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
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }

    /** 가격 행 저장소 스텁 — 신규 저장 시 id를 부여하고 삭제 대상을 기록한다. */
    private static final class MapProductPriceRepository implements ProductPriceRepository {

        private final Map<Long, ProductPrice> rows = new LinkedHashMap<>();
        private final List<Long> deleted = new ArrayList<>();
        private final AtomicLong sequence = new AtomicLong(1000L);

        private void seed(ProductPrice price) {
            rows.put(price.getId(), price);
        }

        @Override
        public ProductPrice save(ProductPrice productPrice) {
            if (productPrice.getId() != null) {
                rows.put(productPrice.getId(), productPrice);
                return productPrice;
            }
            // 신규 저장은 id가 부여된 새 인스턴스로 재구성해 돌려준다(JPA 어댑터와 같은 계약).
            long id = sequence.incrementAndGet();
            ProductPrice saved = ProductPrice.reconstitute(
                id,
                productPrice.getProductId(),
                productPrice.getPriceName(),
                productPrice.getDeliveryPrice(),
                productPrice.getStorePrice(),
                productPrice.getPickupPrice(),
                productPrice.getSort(),
                productPrice.getPickupPriceSetAt(),
                null,
                null
            );
            rows.put(id, saved);
            return saved;
        }

        @Override
        public Optional<ProductPrice> findById(ProductPriceId id) {
            return Optional.ofNullable(rows.get(id.value()));
        }

        @Override
        public List<ProductPrice> findAllByProductId(ProductId productId) {
            return rows.values().stream()
                .filter(price -> price.getProductId().equals(productId))
                .sorted(Comparator.comparingInt(ProductPrice::getSort))
                .toList();
        }

        @Override
        public List<ProductPrice> findAllByShopId(ShopId shopId) {
            // 이 스텁은 가게 필터를 두지 않는다 — 재인증 판정 테스트가 "저장된 모든 행"을 보게 해
            // 위반 행 1건이 인증을 내리는지를 확인하는 것이 목적이다.
            return List.copyOf(rows.values());
        }

        @Override
        public void deleteAllByIdIn(List<ProductPriceId> ids) {
            for (ProductPriceId id : ids) {
                rows.remove(id.value());
                deleted.add(id.value());
            }
        }
    }

    /** 가게 인증 플래그를 메모리로 들고 있는 포트 스텁. */
    private static final class RecordingVerificationPort implements StorePriceVerificationPort {

        private boolean verified;

        @Override
        public boolean isStorePriceVerified(Long shopId) {
            return this.verified;
        }

        @Override
        public void verifyStorePrice(Long shopId) {
            this.verified = true;
        }

        @Override
        public void clearStorePriceVerification(Long shopId) {
            this.verified = false;
        }
    }
}
