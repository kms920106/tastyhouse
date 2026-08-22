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
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 주문 옵션 검증의 순수 단위 테스트.
 *
 * <p><b>여기서 막는 것들은 전부 "프론트만 막고 서버는 통과시키던" 결함이다</b> — 필수 옵션그룹을 비운
 * 주문, 숨긴·품절 옵션을 실은 주문. 3단계 보증금이 도입되면 후자는 "보증금 옵션을 숨겨 보증금 없이
 * 주문"하는 경로가 되므로, 이 테스트가 그 우회를 영구히 봉인한다.
 */
class OrderProductValidationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 12, 0);
    private static final long PRODUCT_ID = 10L;
    private static final long REQUIRED_GROUP = 100L;
    private static final long DEPOSIT_GROUP = 300L;

    @Test
    @DisplayName("★ 필수 옵션그룹을 비운 주문은 거부한다 — 서버가 처음으로 이 계약을 강제한다")
    void validate_requiredGroupNotSelected_rejected() {
        Fixture fixture = requiredGroupFixture();

        assertThatThrownBy(() -> fixture.validate(List.of()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID);
    }

    @Test
    @DisplayName("필수 옵션그룹을 채우면 통과한다")
    void validate_requiredGroupSelected_passes() {
        Fixture fixture = requiredGroupFixture();

        assertThatCode(() -> fixture.validate(List.of(option(REQUIRED_GROUP, 101L))))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ minSelect 미달은 거부한다")
    void validate_belowMinSelect_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(200L, false, 2, 3);
        fixture.addOption(201L, 200L);
        fixture.addOption(202L, 200L);

        assertThatThrownBy(() -> fixture.validate(List.of(option(200L, 201L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID);
    }

    @Test
    @DisplayName("★ maxSelect 초과는 거부한다")
    void validate_aboveMaxSelect_rejected() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(200L, false, 0, 1);
        fixture.addOption(201L, 200L);
        fixture.addOption(202L, 200L);

        assertThatThrownBy(() -> fixture.validate(List.of(option(200L, 201L), option(200L, 202L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID);
    }

    @Test
    @DisplayName("경계값(min·max와 정확히 같은 개수)은 통과한다")
    void validate_boundaryCounts_pass() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(200L, false, 2, 2);
        fixture.addOption(201L, 200L);
        fixture.addOption(202L, 200L);

        assertThatCode(() -> fixture.validate(List.of(option(200L, 201L), option(200L, 202L))))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("선택하지 않은 비필수 그룹에는 minSelect 하한을 적용하지 않는다 — '고르지 않음'이 유효하다")
    void validate_optionalGroupNotSelected_passes() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(200L, false, 2, 3);
        fixture.addOption(201L, 200L);

        assertThatCode(() -> fixture.validate(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 숨긴 옵션을 실은 주문은 거부한다 — 존재 여부를 노출하지 않도록 NOT_FOUND로 답한다")
    void validate_hiddenOption_rejected() {
        Fixture fixture = requiredGroupFixture();
        fixture.options.get(101L).hide();

        assertThatThrownBy(() -> fixture.validate(List.of(option(REQUIRED_GROUP, 101L))))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("★ 품절 옵션을 실은 주문은 거부한다")
    void validate_soldOutOption_rejected() {
        Fixture fixture = requiredGroupFixture();
        fixture.options.get(101L).markSoldOut();

        assertThatThrownBy(() -> fixture.validate(List.of(option(REQUIRED_GROUP, 101L))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_PRODUCT_SOLD_OUT);
    }

    @Test
    @DisplayName("숨긴 옵션그룹은 선택하지 않아도 통과한다 — 손님 메뉴판에 없기 때문이다")
    void validate_hiddenRequiredGroup_ignored() {
        Fixture fixture = requiredGroupFixture();
        fixture.groups.get(REQUIRED_GROUP).hide();

        assertThatCode(() -> fixture.validate(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 보증금은 클라이언트 값이 아니라 옵션의 cupCount × 300원에서 나온다")
    void validate_depositAmountComesFromCupCount() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(DEPOSIT_GROUP, false, 0, 1, ProductOptionGroupType.CUP_DEPOSIT);
        fixture.addDepositOption(301L, 2, null);

        List<OrderProductSnapshot> snapshots = fixture.validate(List.of(option(DEPOSIT_GROUP, 301L)));

        OrderProductOptionSnapshot option = snapshots.getFirst().options().getFirst();
        assertThat(option.optionGroupType()).isEqualTo("CUP_DEPOSIT");
        assertThat(option.cupCount()).isEqualTo(2);
        assertThat(option.depositAmount()).isEqualTo(600);
    }

    @Test
    @DisplayName("★ 개인컵 옵션은 보증금이 0이고 할인 금액만 갖는다 — 보증금이 아니라 할인 축이다")
    void validate_personalCupOption_hasNoDeposit() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(DEPOSIT_GROUP, false, 0, 1, ProductOptionGroupType.CUP_DEPOSIT);
        fixture.addDepositOption(302L, null, 300);

        List<OrderProductSnapshot> snapshots = fixture.validate(List.of(option(DEPOSIT_GROUP, 302L)));

        OrderProductOptionSnapshot option = snapshots.getFirst().options().getFirst();
        assertThat(option.depositAmount()).isZero();
        assertThat(option.personalCupDiscountAmount()).isEqualTo(300);
    }

    @Test
    @DisplayName("일반 옵션은 보증금이 0이다 — 기존 주문 동작이 그대로 유지된다")
    void validate_normalOption_hasNoDeposit() {
        Fixture fixture = requiredGroupFixture();

        List<OrderProductSnapshot> snapshots = fixture.validate(List.of(option(REQUIRED_GROUP, 101L)));

        OrderProductOptionSnapshot option = snapshots.getFirst().options().getFirst();
        assertThat(option.optionGroupType()).isEqualTo("NORMAL");
        assertThat(option.depositAmount()).isZero();
        assertThat(option.cupCount()).isNull();
    }

    private static Fixture requiredGroupFixture() {
        Fixture fixture = new Fixture();
        fixture.addProduct();
        fixture.addGroup(REQUIRED_GROUP, true, 1, 1);
        fixture.addOption(101L, REQUIRED_GROUP);
        return fixture;
    }

    private static OrderLineOptionSelection option(Long groupId, Long optionId) {
        return OrderLineOptionSelection.of(groupId, optionId);
    }

    private static final class Fixture {

        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final Map<Long, ProductOptionGroup> groups = new LinkedHashMap<>();
        private final Map<Long, ProductOption> options = new LinkedHashMap<>();
        private final FakeProductOptionGroupLinkRepository links = new FakeProductOptionGroupLinkRepository();
        private final OrderProductValidationService service;

        private Fixture() {
            this.service = new OrderProductValidationService(
                new StubProductRepository(products),
                new MapOptionGroupRepository(groups),
                new MapOptionRepository(options),
                new NoImageRepository(),
                links,
                new NoExposureHourRepository(),
                new ProductExposureCalculator(),
                new CupDepositPolicy()
            );
        }

        private List<OrderProductSnapshot> validate(List<OrderLineOptionSelection> selectedOptions) {
            return service.validate(
                List.of(OrderLineSelection.of(PRODUCT_ID, 1, selectedOptions)), NOW);
        }

        private void addProduct() {
            products.put(PRODUCT_ID, Product.reconstitute(
                PRODUCT_ID, ShopId.of(1L), null, "메뉴", null, 1000, null, null, 0,
                false, null, false, null, true, 0,
                false, false, null, false, null, null, null, null, null, null
            ));
        }

        private void addGroup(Long id, boolean required, Integer minSelect, Integer maxSelect) {
            addGroup(id, required, minSelect, maxSelect, ProductOptionGroupType.NORMAL);
        }

        private void addGroup(
            Long id,
            boolean required,
            Integer minSelect,
            Integer maxSelect,
            ProductOptionGroupType groupType
        ) {
            groups.put(id, ProductOptionGroup.reconstitute(
                id, ProductId.of(PRODUCT_ID), "그룹" + id, null, required, false,
                minSelect, maxSelect, 0, true, groupType
            ));
            links.seed(PRODUCT_ID, id, groups.size() - 1);
        }

        private void addOption(Long id, Long groupId) {
            options.put(id, ProductOption.reconstitute(
                id, ProductOptionGroupId.of(groupId), "옵션" + id, 0, 0, false, null, true, null, null));
        }

        private void addDepositOption(Long id, Integer cupCount, Integer personalCupDiscount) {
            options.put(id, ProductOption.reconstitute(
                id, ProductOptionGroupId.of(DEPOSIT_GROUP), "옵션" + id, 0, 0, false, null, true,
                cupCount, personalCupDiscount));
        }
    }

    private static final class MapOptionGroupRepository implements ProductOptionGroupRepository {

        private final Map<Long, ProductOptionGroup> groups;

        private MapOptionGroupRepository(Map<Long, ProductOptionGroup> groups) {
            this.groups = groups;
        }

        @Override
        public ProductOptionGroup save(ProductOptionGroup group) {
            groups.put(group.getId(), group);
            return group;
        }

        @Override
        public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
            return Optional.ofNullable(groups.get(id.value()));
        }

        @Override
        public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
            return ids.stream()
                .map(id -> groups.get(id.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }
    }

    private static final class MapOptionRepository implements ProductOptionRepository {

        private final Map<Long, ProductOption> options;

        private MapOptionRepository(Map<Long, ProductOption> options) {
            this.options = options;
        }

        @Override
        public ProductOption save(ProductOption option) {
            options.put(option.getId(), option);
            return option;
        }

        @Override
        public Optional<ProductOption> findById(ProductOptionId id) {
            return Optional.ofNullable(options.get(id.value()));
        }

        @Override
        public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return options.values().stream()
                .filter(option -> option.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
            return ids.stream()
                .map(id -> options.get(id.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }

        @Override
        public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
            return List.of();
        }
    }

    /** 대표 이미지가 없는 상태를 나타내는 스텁 — 이 테스트는 이미지를 검증하지 않는다. */
    private static final class NoImageRepository implements ProductImageRepository {

        @Override
        public UploadedFileId findRepresentativeImageFileId(ProductId productId) {
            return null;
        }

        @Override
        public ProductImage save(ProductImage productImage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ProductImage> findById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ProductImage> findAllByProductId(ProductId productId) {
            return List.of();
        }

        @Override
        public void delete(ProductImage productImage) {
            throw new UnsupportedOperationException();
        }
    }

    /** 노출기간 설정이 없는 상태 — 항상 노출로 판정된다. */
    private static final class NoExposureHourRepository implements ProductExposureHourRepository {

        @Override
        public List<ProductExposureHour> saveAll(List<ProductExposureHour> hours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ProductExposureHour> findAllByProductId(ProductId productId) {
            return List.of();
        }

        @Override
        public void deleteAllByProductId(ProductId productId) {
            throw new UnsupportedOperationException();
        }
    }
}
