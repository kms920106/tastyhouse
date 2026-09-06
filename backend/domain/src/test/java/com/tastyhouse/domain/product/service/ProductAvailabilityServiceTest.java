package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductAvailabilityServiceTest {
    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 12, 0);

    @Test
    @DisplayName("마지막 노출 메뉴는 숨길 수 없다 — 메뉴판에 최소 1개가 남아야 한다")
    void hideProducts_lastVisible_rejected() {
        Product only = product(10L, "떡볶이", true, false, 1);
        Fixture fixture = Fixture.withProducts(List.of(only), 1, 0);

        ProductAvailabilityChangeResult result =
            fixture.service.hideProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.succeeded()).isEmpty();
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode()).isEqualTo(ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        assertThat(only.isVisible()).isTrue();
    }

    @Test
    @DisplayName("마지막 추천 메뉴는 숨길 수 없다 — 추천 메뉴가 최소 1개 남아야 한다")
    void hideProducts_lastRepresentative_rejected() {
        Product representative = product(10L, "대표메뉴", true, true, 1);
        Fixture fixture = Fixture.withProducts(List.of(representative), 3, 1);

        ProductAvailabilityChangeResult result =
            fixture.service.hideProducts(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(result.succeeded()).isEmpty();
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        assertThat(representative.isVisible()).isTrue();
    }

    @Test
    @DisplayName("옵션 품절은 옵션그룹의 minSelect 개수만큼 판매 중인 옵션을 남긴다")
    void markOptionsSoldOut_minSelectViolation_rejectsExcess() {
        ProductOption first = option(100L, "곱빼기", 1);
        ProductOption second = option(101L, "치즈추가", 2);
        Fixture fixture = Fixture.withOptions(List.of(first, second), optionGroup(1));

        ProductAvailabilityChangeResult result = fixture.service.markOptionsSoldOut(
            SHOP_ID, List.of(ProductOptionId.of(100L), ProductOptionId.of(101L)), List.of(), null, NOW);

        assertThat(result.succeeded()).containsExactly(100L);
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().id()).isEqualTo(101L);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
        assertThat(first.isSoldOut()).isTrue();
        assertThat(second.isSoldOut()).isFalse();
    }

    @Test
    @DisplayName("minSelect가 null이거나 0이면 하한을 1로 본다 — 옵션그룹이 통째로 선택 불가가 되지 않는다")
    void markOptionsSoldOut_nullMinSelect_treatedAsOne() {
        ProductOption only = option(100L, "곱빼기", 1);
        Fixture fixture = Fixture.withOptions(List.of(only), optionGroup(null));

        ProductAvailabilityChangeResult result = fixture.service.markOptionsSoldOut(
            SHOP_ID, List.of(ProductOptionId.of(100L)), List.of(), null, NOW);

        assertThat(result.succeeded()).isEmpty();
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
    }

    @Test
    @DisplayName("maxSelect도 잔여 하한이 된다 — 최대 3개를 고를 수 있다면 판매중 옵션이 3개 미만이 될 수 없다")
    void markOptionsSoldOut_maxSelectViolation_rejectsExcess() {
        Fixture fixture = Fixture.withOptions(
            List.of(
                option(100L, "곱빼기", 1),
                option(101L, "치즈추가", 2),
                option(102L, "소스추가", 3),
                option(103L, "공기밥", 4)
            ),
            optionGroup(null, 3)
        );

        ProductAvailabilityChangeResult result = fixture.service.markOptionsSoldOut(
            SHOP_ID,
            List.of(
                ProductOptionId.of(100L), ProductOptionId.of(101L),
                ProductOptionId.of(102L), ProductOptionId.of(103L)
            ),
            List.of(), null, NOW
        );

        assertThat(result.succeeded()).containsExactly(100L);
        assertThat(result.failed()).hasSize(3);
        assertThat(result.failed())
            .allSatisfy(failure -> assertThat(failure.errorCode())
                .isEqualTo(ErrorCode.PRODUCT_OPTION_MAX_SELECT_VIOLATION));
    }

    @Test
    @DisplayName("두 제약이 겹칠 때 과잉 거부하지 않는다 — 노출 2개 중 추천이 1개면 실패는 1건이다")
    void hideProducts_overlappingConstraints_doNotOverReject() {
        Product representative = product(10L, "대표메뉴", true, true, 1);
        Product plain = product(11L, "일반메뉴", true, false, 2);
        Fixture fixture = Fixture.withProducts(List.of(representative, plain), 2, 1);

        ProductAvailabilityChangeResult result =
            fixture.service.hideProducts(SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)));

        assertThat(result.failed()).hasSize(1);
        assertThat(result.succeeded()).hasSize(1);

        assertThat(representative.isVisible()).isTrue();
        assertThat(plain.isVisible()).isFalse();
    }

    @Test
    @DisplayName("succeeded와 failed는 서로 겹치지 않는다")
    void hideProducts_succeededAndFailedAreDisjoint() {
        Product representative = product(10L, "대표메뉴", true, true, 1);
        Product plain = product(11L, "일반메뉴", true, false, 2);
        Fixture fixture = Fixture.withProducts(List.of(representative, plain), 2, 1);

        ProductAvailabilityChangeResult result =
            fixture.service.hideProducts(SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)));

        List<Long> failedIds = result.failed().stream().map(ProductAvailabilityFailure::id).toList();
        assertThat(result.succeeded()).doesNotContainAnyElementsOf(failedIds);
        assertThat(failedIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("노출 메뉴 2개를 순서를 바꿔 요청해도 같은 결과다 — 최종 상태 기준 판정")
    void hideProducts_isOrderIndependent() {
        List<Long> forwardSucceeded = hideTwoVisible(false);
        List<Long> reverseSucceeded = hideTwoVisible(true);

        assertThat(forwardSucceeded).isEqualTo(reverseSucceeded);
        assertThat(forwardSucceeded).containsExactly(10L);
    }

    private List<Long> hideTwoVisible(boolean reversed) {
        Product first = product(10L, "떡볶이", true, false, 1);
        Product second = product(11L, "튀김", true, false, 2);
        Fixture fixture = Fixture.withProducts(List.of(first, second), 2, 0);

        List<ProductId> ids = reversed
            ? List.of(ProductId.of(11L), ProductId.of(10L))
            : List.of(ProductId.of(10L), ProductId.of(11L));

        return fixture.service.hideProducts(SHOP_ID, ids).succeeded();
    }

    @Test
    @DisplayName("품절 기간은 현재+29분을 거부하고 +30분을 허용한다")
    void validateSoldOutUntil_lowerBoundary() {
        Fixture fixture = Fixture.withProducts(List.of(), 5, 5);

        assertThatThrownBy(() -> fixture.service.validateSoldOutUntil(NOW.plusMinutes(29), NOW))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_SOON);

        fixture.service.validateSoldOutUntil(NOW.plusMinutes(30), NOW);
    }

    @Test
    @DisplayName("품절 기간은 현재+7일을 허용하고 +7일 1분을 거부한다")
    void validateSoldOutUntil_upperBoundary() {
        Fixture fixture = Fixture.withProducts(List.of(), 5, 5);

        fixture.service.validateSoldOutUntil(NOW.plusDays(7), NOW);

        assertThatThrownBy(() -> fixture.service.validateSoldOutUntil(NOW.plusDays(7).plusMinutes(1), NOW))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_FAR);
    }

    @Test
    @DisplayName("soldOutUntil이 null이면(기본값 위임) 기간 검증을 건너뛴다")
    void validateSoldOutUntil_nullSkipsValidation() {
        Fixture fixture = Fixture.withProducts(List.of(), 5, 5);

        fixture.service.validateSoldOutUntil(null, NOW);
    }

    @Test
    @DisplayName("기간 위반은 요청 전체를 거부한다 — 부분실패가 아니라 400이다")
    void markProductsSoldOut_periodViolation_rejectsWholeRequest() {
        Product target = product(10L, "떡볶이", true, false, 1);
        Fixture fixture = Fixture.withProducts(List.of(target), 5, 5);

        assertThatThrownBy(() -> fixture.service.markProductsSoldOut(
            SHOP_ID, List.of(ProductId.of(10L)), NOW.plusMinutes(10), NOW))
            .isInstanceOf(BusinessException.class);

        assertThat(target.isSoldOut()).isFalse();
    }

    @Test
    @DisplayName("품절 해제는 soldOut과 soldOutUntil을 함께 정리한다")
    void releaseProductsSoldOut_clearsBothFields() {
        Product target = product(10L, "떡볶이", true, false, 1);
        target.markSoldOut(NOW.plusHours(5));
        Fixture fixture = Fixture.withProducts(List.of(target), 5, 5);

        fixture.service.releaseProductsSoldOut(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(target.isSoldOut()).isFalse();
        assertThat(target.getSoldOutUntil()).isNull();
    }

    @Test
    @DisplayName("품절 기간 변경은 품절 상태가 아닌 대상을 failed에 담는다(요청 전체를 거부하지 않는다)")
    void changeProductsSoldOutUntil_notSoldOut_goesToFailed() {
        Product soldOut = product(10L, "떡볶이", true, false, 1);
        soldOut.markSoldOut(NOW.plusHours(3));
        Product onSale = product(11L, "튀김", true, false, 2);
        Fixture fixture = Fixture.withProducts(List.of(soldOut, onSale), 5, 5);

        ProductAvailabilityChangeResult result = fixture.service.changeProductsSoldOutUntil(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)), NOW.plusHours(6), NOW);

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().id()).isEqualTo(11L);
        assertThat(result.failed().getFirst().errorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_SOLD_OUT);
        assertThat(soldOut.getSoldOutUntil()).isEqualTo(NOW.plusHours(6));
    }

    @Test
    @DisplayName("ALL 해제는 품절과 숨김을 함께 풀고, 이미 판매중인 항목이 섞여도 실패가 아니다(멱등)")
    void releaseProducts_all_isIdempotent() {
        Product blocked = product(10L, "떡볶이", false, false, 1);
        blocked.markSoldOut(NOW.plusHours(3));
        Product healthy = product(11L, "튀김", true, false, 2);
        Fixture fixture = Fixture.withProducts(List.of(blocked, healthy), 5, 5);

        ProductAvailabilityChangeResult result = fixture.service.releaseProducts(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L)), ReleaseTarget.ALL);

        assertThat(result.succeeded()).containsExactly(10L, 11L);
        assertThat(result.failed()).isEmpty();
        assertThat(blocked.isSoldOut()).isFalse();
        assertThat(blocked.getSoldOutUntil()).isNull();
        assertThat(blocked.isVisible()).isTrue();
    }

    @Test
    @DisplayName("미존재·타 가게 소유 id는 PRODUCT_NOT_FOUND로 실패 처리된다")
    void loadProducts_unknownId_failsWithNotFound() {
        Product owned = product(10L, "떡볶이", true, false, 1);
        Fixture fixture = Fixture.withProducts(List.of(owned), 5, 5);

        ProductAvailabilityChangeResult result = fixture.service.releaseProductsSoldOut(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(999L)));

        assertThat(result.succeeded()).containsExactly(10L);
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().id()).isEqualTo(999L);
        assertThat(result.failed().getFirst().errorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("공통 옵션은 자기 그룹의 옵션만 세어 minSelect를 판정한다(일반 옵션과 별도 테이블)")
    void markOptionsSoldOut_commonOptionsCountedSeparately() {
        ProductCommonOption first = commonOption(200L, "포크", 1);
        ProductCommonOption second = commonOption(201L, "물티슈", 2);
        Fixture fixture = Fixture.withCommonOptions(List.of(first, second), commonOptionGroup());

        ProductAvailabilityChangeResult result = fixture.service.markOptionsSoldOut(
            SHOP_ID, List.of(),
            List.of(ProductCommonOptionId.of(200L), ProductCommonOptionId.of(201L)), null, NOW);

        assertThat(result.succeeded()).containsExactly(200L);
        assertThat(result.failed()).hasSize(1);
        assertThat(result.failed().getFirst().errorCode())
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
    }

    private static Product product(Long id, String name, boolean visible, boolean representative, int sort) {
        return Product.reconstitute(
            id, SHOP_ID, ProductCategoryId.of(2L), name, "설명", 10000,
            null, null, 0, representative, null, false, null, visible, sort, false,
            false, null, false, null, null, null, null, null, null
        );
    }

    private static ProductOption option(Long id, String name, int sort) {
        return ProductOption.reconstitute(
            id, ProductOptionGroupId.of(20L), name, 1000, sort, false, null, true,
            null,
            null
        );
    }

    private static ProductCommonOption commonOption(Long id, String name, int sort) {
        return ProductCommonOption.reconstitute(
            id, ProductOptionGroupId.of(30L), name, 0, sort, false, null, true);
    }

    private static ProductOptionGroup optionGroup(Integer minSelect) {
        return optionGroup(minSelect, null);
    }

    private static ProductOptionGroup optionGroup(Integer minSelect, Integer maxSelect) {
        return ProductOptionGroup.reconstitute(
            20L, ProductId.of(500L), "그룹", "설명", true, false, minSelect, maxSelect, 1, true,
            ProductOptionGroupType.NORMAL
        );
    }

    private static ProductCommonOptionGroup commonOptionGroup() {
        return ProductCommonOptionGroup.reconstitute(
            30L, ProductId.of(500L), "공통그룹", "설명", true, false, 1, null, 1, true);
    }

    private static final class Fixture {
        private final ProductAvailabilityService service;

        private Fixture(
            List<Product> products,
            long visibleCount,
            long visibleRepresentativeCount,
            List<ProductOption> options,
            List<ProductCommonOption> commonOptions,
            List<ProductOptionGroup> optionGroups,
            List<ProductCommonOptionGroup> commonOptionGroups
        ) {
            List<Product> owned = new ArrayList<>(products);
            owned.add(product(500L, "옵션소유상품", true, false, 99));

            List<ProductOptionGroupLink> optionGroupLinks = optionGroups.stream()
                .map(group -> ProductOptionGroupLink.reconstitute(
                    group.getId(), ProductId.of(500L), ProductOptionGroupId.of(group.getId()), 1))
                .toList();
            List<ProductCommonOptionGroupLink> commonOptionGroupLinks = commonOptionGroups.stream()
                .map(group -> ProductCommonOptionGroupLink.reconstitute(
                    group.getId(), ProductId.of(500L), ProductOptionGroupId.of(group.getId()), 1))
                .toList();

            this.service = new ProductAvailabilityService(
                new ProductRepositoryStub(owned, visibleCount, visibleRepresentativeCount),
                new ProductOptionRepositoryStub(options),
                new ProductCommonOptionRepositoryStub(commonOptions),
                new ProductOptionGroupRepositoryStub(optionGroups),
                new ProductCommonOptionGroupRepositoryStub(commonOptionGroups),
                new ProductOptionGroupLinkRepositoryStub(optionGroupLinks),
                new ProductCommonOptionGroupLinkRepositoryStub(commonOptionGroupLinks)
            );
        }

        private static Fixture withProducts(
            List<Product> products,
            long visibleCount,
            long visibleRepresentativeCount
        ) {
            return new Fixture(products, visibleCount, visibleRepresentativeCount,
                List.of(), List.of(), List.of(), List.of());
        }

        private static Fixture withOptions(List<ProductOption> options, ProductOptionGroup group) {
            return new Fixture(List.of(), 5, 5, options, List.of(), List.of(group), List.of());
        }

        private static Fixture withCommonOptions(
            List<ProductCommonOption> options,
            ProductCommonOptionGroup group
        ) {
            return new Fixture(List.of(), 5, 5, List.of(), options, List.of(), List.of(group));
        }
    }

    private record ProductRepositoryStub(
        List<Product> products,
        long visibleCount,
        long visibleRepresentativeCount
    ) implements ProductRepository {
        @Override
        public Optional<Product> findById(ProductId id) {
            return products.stream().filter(product -> product.getId().equals(id.value())).findFirst();
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            List<Long> raw = ids.stream().map(ProductId::value).toList();
            return products.stream().filter(product -> raw.contains(product.getId())).toList();
        }

        @Override
        public long countVisibleByShopId(ShopId shopId) {
            return visibleCount;
        }

        @Override
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            return visibleRepresentativeCount;
        }

        @Override
        public long countRepresentativeByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return List.of();
        }

        @Override
        public Optional<Product> findByIdIncludingDeleted(ProductId id) {
            return findById(id);
        }

        @Override
        public boolean existsByShopIdAndName(ShopId shopId, String name) {
            return products.stream().anyMatch(product -> product.getName().equals(name));
        }

        @Override
        public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
            return products.stream()
                .filter(product -> !product.getId().equals(excludedId.value()))
                .anyMatch(product -> product.getName().equals(name));
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

    private record ProductOptionRepositoryStub(List<ProductOption> options) implements ProductOptionRepository {
        @Override
        public Optional<ProductOption> findById(ProductOptionId id) {
            return options.stream().filter(option -> option.getId().equals(id.value())).findFirst();
        }

        @Override
        public ProductOption save(ProductOption productOption) {
            return productOption;
        }

        @Override
        public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
            List<Long> raw = ids.stream().map(ProductOptionId::value).toList();
            return options.stream().filter(option -> raw.contains(option.getId())).toList();
        }

        @Override
        public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return options.stream()
                .filter(option -> option.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return List.of();
        }
    }

    private record ProductCommonOptionRepositoryStub(
        List<ProductCommonOption> options
    ) implements ProductCommonOptionRepository {
        @Override
        public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
            return options.stream().filter(option -> option.getId().equals(id.value())).findFirst();
        }

        @Override
        public ProductCommonOption save(ProductCommonOption productCommonOption) {
            return productCommonOption;
        }

        @Override
        public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
            List<Long> raw = ids.stream().map(ProductCommonOptionId::value).toList();
            return options.stream().filter(option -> raw.contains(option.getId())).toList();
        }

        @Override
        public List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return options.stream()
                .filter(option -> option.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return List.of();
        }
    }

    private record ProductOptionGroupRepositoryStub(
        List<ProductOptionGroup> groups
    ) implements ProductOptionGroupRepository {
        @Override
        public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
            return groups.stream().filter(group -> group.getId().equals(id.value())).findFirst();
        }

        @Override
        public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
            return productOptionGroup;
        }

        @Override
        public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
            List<Long> raw = ids.stream().map(ProductOptionGroupId::value).toList();
            return groups.stream().filter(group -> raw.contains(group.getId())).toList();
        }
    }

    private record ProductCommonOptionGroupRepositoryStub(
        List<ProductCommonOptionGroup> groups
    ) implements ProductCommonOptionGroupRepository {
        @Override
        public ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup) {
            return productCommonOptionGroup;
        }

        @Override
        public List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
            List<Long> raw = ids.stream().map(ProductOptionGroupId::value).toList();
            return groups.stream().filter(group -> raw.contains(group.getId())).toList();
        }
    }

    private record ProductOptionGroupLinkRepositoryStub(
        List<ProductOptionGroupLink> links
    ) implements ProductOptionGroupLinkRepository {
        @Override
        public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
            return links.stream()
                .filter(link -> optionGroupIds.contains(link.getOptionGroupId()))
                .toList();
        }

        @Override
        public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return links.stream()
                .filter(link -> link.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
            return links.stream()
                .filter(link -> link.getProductId().equals(productId))
                .toList();
        }

        @Override
        public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
            ProductId productId,
            ProductOptionGroupId optionGroupId
        ) {
            return links.stream()
                .filter(link -> link.getProductId().equals(productId)
                    && link.getOptionGroupId().equals(optionGroupId))
                .findFirst();
        }

        @Override
        public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
            return findByProductIdAndOptionGroupId(productId, optionGroupId).isPresent();
        }

        @Override
        public ProductOptionGroupLink save(ProductOptionGroupLink link) {
            return link;
        }

        @Override
        public void delete(ProductOptionGroupLink link) {
            throw new UnsupportedOperationException();
        }
    }

    private record ProductCommonOptionGroupLinkRepositoryStub(
        List<ProductCommonOptionGroupLink> links
    ) implements ProductCommonOptionGroupLinkRepository {
        @Override
        public List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(
            List<ProductOptionGroupId> optionGroupIds
        ) {
            return links.stream()
                .filter(link -> optionGroupIds.contains(link.getOptionGroupId()))
                .toList();
        }

        @Override
        public List<ProductCommonOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return links.stream()
                .filter(link -> link.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductCommonOptionGroupLink> findAllByProductId(ProductId productId) {
            return links.stream()
                .filter(link -> link.getProductId().equals(productId))
                .toList();
        }

        @Override
        public Optional<ProductCommonOptionGroupLink> findByProductIdAndOptionGroupId(
            ProductId productId,
            ProductOptionGroupId optionGroupId
        ) {
            return links.stream()
                .filter(link -> link.getProductId().equals(productId)
                    && link.getOptionGroupId().equals(optionGroupId))
                .findFirst();
        }

        @Override
        public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
            return findByProductIdAndOptionGroupId(productId, optionGroupId).isPresent();
        }

        @Override
        public ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link) {
            return link;
        }

        @Override
        public void delete(ProductCommonOptionGroupLink link) {
            throw new UnsupportedOperationException();
        }
    }
}
