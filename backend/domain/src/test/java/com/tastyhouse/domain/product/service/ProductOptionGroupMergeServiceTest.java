package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 옵션그룹 합치기의 순수 단위 테스트.
 *
 * <p><b>이 테스트가 지키는 것</b>은 합치기의 정의 그 자체다 — 기준 그룹은 손대지 않고, 흡수 그룹은
 * 행을 남긴 채 감추며, <b>옵션을 union하지 않는다</b>. 합치기는 되돌릴 수 없으므로 회귀가 나면
 * 데이터로 복구할 수단이 없고, 그래서 이 규칙들이 코드가 아니라 테스트로 못박혀 있어야 한다.
 */
class ProductOptionGroupMergeServiceTest {

    private static final ShopId MY_SHOP = ShopId.of(1L);
    private static final ShopId OTHER_SHOP = ShopId.of(2L);
    private static final CeoId ACTOR = CeoId.of(7L);

    private static final long BASE_GROUP = 100L;
    private static final long TARGET_GROUP = 200L;

    @Test
    @DisplayName("★ 기준 그룹의 이름·선택 제약·옵션은 합친 뒤에도 변경되지 않는다")
    void merge_doesNotTouchBaseGroup() {
        Fixture fixture = defaultFixture();
        ProductOptionGroup base = fixture.groups.get(BASE_GROUP);
        List<Long> baseOptionIdsBefore = fixture.optionIdsOf();

        fixture.merge(TARGET_GROUP);

        assertThat(base.getName()).isEqualTo("기준그룹");
        assertThat(base.getMinSelect()).isEqualTo(1);
        assertThat(base.getMaxSelect()).isEqualTo(1);
        assertThat(base.isVisible()).isTrue();
        // union 회귀 가드 — 합치기 전후로 기준 그룹의 옵션 집합이 완전히 같아야 한다.
        assertThat(fixture.optionIdsOf()).isEqualTo(baseOptionIdsBefore);
    }

    @Test
    @DisplayName("★ 흡수 그룹은 행이 남은 채 감춰진다 — 하드 삭제는 과거 주문 참조를 끊는다")
    void merge_hidesTargetGroupButKeepsRow() {
        Fixture fixture = defaultFixture();

        fixture.merge(TARGET_GROUP);

        ProductOptionGroup target = fixture.groups.get(TARGET_GROUP);
        assertThat(target).isNotNull();
        assertThat(target.isVisible()).isFalse();
    }

    @Test
    @DisplayName("★ 흡수 그룹의 옵션은 기준으로 재부모화되지 않고 각각 감춰진다")
    void merge_doesNotReparentTargetOptions() {
        Fixture fixture = defaultFixture();

        fixture.merge(TARGET_GROUP);

        List<ProductOption> targetOptions = fixture.options.findAllByOptionGroupId(
            ProductOptionGroupId.of(TARGET_GROUP));
        assertThat(targetOptions).isNotEmpty();
        assertThat(targetOptions).allSatisfy(option -> {
            assertThat(option.getOptionGroupId()).isEqualTo(ProductOptionGroupId.of(TARGET_GROUP));
            assertThat(option.isVisible()).isFalse();
        });
    }

    @Test
    @DisplayName("링크가 기준 그룹으로 옮겨지고 원래 sort가 보존된다")
    void merge_relinksPreservingSort() {
        Fixture fixture = defaultFixture();

        fixture.merge(TARGET_GROUP);

        // 메뉴 20은 흡수 그룹에만 연결돼 있었으므로 이제 기준 그룹을 본다.
        List<ProductOptionGroupLink> linksOfProduct20 = fixture.links.findAllByProductId(ProductId.of(20L));
        assertThat(linksOfProduct20).hasSize(1);
        assertThat(linksOfProduct20.getFirst().getOptionGroupId())
            .isEqualTo(ProductOptionGroupId.of(BASE_GROUP));
        // 링크가 하나뿐이므로 재정규화로 sort는 0이 된다.
        assertThat(linksOfProduct20.getFirst().getSort()).isZero();
    }

    @Test
    @DisplayName("링크가 사라진 메뉴의 남은 sort가 0..N-1로 재정규화된다")
    void merge_renumbersRemainingLinks() {
        Fixture fixture = defaultFixture();
        // 메뉴 20에 다른 그룹(300)을 sort=1로 하나 더 붙인다 → 흡수 그룹(sort=0)이 옮겨간 뒤 재정규화 대상.
        fixture.addGroup(300L, "다른그룹", 0);
        fixture.links.seed(20L, 300L, 1);

        fixture.merge(TARGET_GROUP);

        List<ProductOptionGroupLink> links = fixture.links.findAllByProductId(ProductId.of(20L));
        assertThat(links).hasSize(2);
        assertThat(links.stream().map(ProductOptionGroupLink::getSort)).containsExactly(0, 1);
    }

    @Test
    @DisplayName("흡수 그룹당 이력 1행이 남고 흡수 시점 그룹명이 스냅샷된다")
    void merge_appendsHistoryPerTarget() {
        Fixture fixture = defaultFixture();

        fixture.merge(TARGET_GROUP);

        assertThat(fixture.histories).hasSize(1);
        ProductOptionGroupMergeHistory history = fixture.histories.getFirst();
        assertThat(history.getShopId()).isEqualTo(MY_SHOP);
        assertThat(history.getBaseOptionGroupId()).isEqualTo(ProductOptionGroupId.of(BASE_GROUP));
        assertThat(history.getMergedOptionGroupId()).isEqualTo(ProductOptionGroupId.of(TARGET_GROUP));
        assertThat(history.getMergedGroupName()).isEqualTo("흡수그룹");
        assertThat(history.getEntryType()).isEqualTo(ProductOptionGroupMergeEntryType.RECOMMENDED);
        assertThat(history.getActorCeoId()).isEqualTo(ACTOR);
    }

    @Test
    @DisplayName("중복 id를 실어 보내도 한 번만 처리된다 — 이력이 2행 쌓이지 않는다")
    void merge_deduplicatesTargetIds() {
        Fixture fixture = defaultFixture();

        fixture.merge(TARGET_GROUP, TARGET_GROUP);

        assertThat(fixture.histories).hasSize(1);
    }

    @Test
    @DisplayName("대상이 비어 있으면 거부한다")
    void merge_emptyTargets_rejected() {
        Fixture fixture = defaultFixture();

        assertThatThrownBy(fixture::merge)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
    }

    @Test
    @DisplayName("기준 그룹을 흡수 대상에 포함하면 거부한다")
    void merge_baseIncluded_rejected() {
        Fixture fixture = defaultFixture();

        assertThatThrownBy(() -> fixture.merge(BASE_GROUP))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_BASE_INCLUDED);
    }

    @Test
    @DisplayName("이미 감춰진 그룹은 합칠 수 없다")
    void merge_hiddenTarget_rejected() {
        Fixture fixture = defaultFixture();
        fixture.groups.get(TARGET_GROUP).hide();

        assertThatThrownBy(() -> fixture.merge(TARGET_GROUP))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET);
    }

    @Test
    @DisplayName("★ 같은 메뉴에 연결된 두 그룹은 합칠 수 없다 — 그 메뉴의 링크가 조용히 줄어든다")
    void merge_sameProductLinked_rejected() {
        Fixture fixture = defaultFixture();
        // 기준 그룹이 걸린 메뉴 10에 흡수 대상도 연결한다.
        fixture.links.seed(10L, TARGET_GROUP, 1);

        assertThatThrownBy(() -> fixture.merge(TARGET_GROUP))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED);
    }

    @Test
    @DisplayName("★ 흡수 대상 둘이 같은 메뉴를 공유해도 거부한다 — base-vs-각각이 아니라 집합 전체 pairwise")
    void merge_targetsSharingProduct_rejected() {
        Fixture fixture = defaultFixture();
        fixture.addGroup(300L, "또다른흡수", 1);
        fixture.addOption(301L, 300L, "옵션");
        // 메뉴 20을 흡수 대상 둘이 공유한다(기준 그룹은 무관).
        fixture.links.seed(20L, 300L, 1);

        assertThatThrownBy(() -> fixture.merge(TARGET_GROUP, 300L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED);
    }

    @Test
    @DisplayName("연결이 0건인 고아 그룹은 미존재와 같이 404로 거부한다 — 존재 여부를 흘리지 않는다")
    void merge_orphanGroup_rejected() {
        Fixture fixture = defaultFixture();
        fixture.addGroup(400L, "고아", 1);

        assertThatThrownBy(() -> fixture.merge(400L))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 가게 그룹은 거부한다")
    void merge_otherShopGroup_rejected() {
        Fixture fixture = defaultFixture();
        fixture.addProduct(30L, OTHER_SHOP);
        fixture.addGroup(500L, "남의그룹", 1);
        fixture.addOption(501L, 500L, "옵션");
        fixture.links.seed(30L, 500L, 0);

        assertThatThrownBy(() -> fixture.merge(500L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
    }

    @Test
    @DisplayName("보증금 옵션그룹은 일반 옵션그룹과 합칠 수 없다 — 금액의 성격이 다르다")
    void merge_typeMismatch_rejected() {
        Fixture fixture = defaultFixture();
        fixture.addGroup(600L, "보증금그룹", 0, ProductOptionGroupType.CUP_DEPOSIT);
        fixture.addOption(601L, 600L, "일회용컵");
        fixture.links.seed(20L, 600L, 1);
        // 같은 메뉴 공유 검증에 먼저 걸리지 않도록 별도 메뉴에 연결한다.
        fixture.links.delete(fixture.links.findByProductIdAndOptionGroupId(
            ProductId.of(20L), ProductOptionGroupId.of(600L)).orElseThrow());
        fixture.addProduct(40L, MY_SHOP);
        fixture.links.seed(40L, 600L, 0);

        assertThatThrownBy(() -> fixture.merge(600L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH);
    }

    @Test
    @DisplayName("기준 그룹이 자기 최소 선택 개수를 못 채우면 거부한다 — 흡수 메뉴들이 주문 불가가 된다")
    void merge_baseCannotSatisfyMinSelect_rejected() {
        Fixture fixture = defaultFixture();
        // 기준 그룹의 유일한 판매중 옵션을 감춘다(minSelect=1을 못 채운다).
        fixture.options.findAllByOptionGroupId(ProductOptionGroupId.of(BASE_GROUP))
            .forEach(ProductOption::hide);

        assertThatThrownBy(() -> fixture.merge(TARGET_GROUP))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
    }

    /**
     * 기본 시나리오: 같은 가게의 메뉴 10(기준 그룹 연결)과 메뉴 20(흡수 그룹 연결).
     * 두 그룹은 같은 메뉴를 공유하지 않으므로 합치기가 가능한 상태다.
     */
    private static Fixture defaultFixture() {
        Fixture fixture = new Fixture();
        fixture.addProduct(10L, MY_SHOP);
        fixture.addProduct(20L, MY_SHOP);

        fixture.addGroup(BASE_GROUP, "기준그룹", 1);
        fixture.addOption(101L, BASE_GROUP, "기본");
        fixture.links.seed(10L, BASE_GROUP, 0);

        fixture.addGroup(TARGET_GROUP, "흡수그룹", 1);
        fixture.addOption(201L, TARGET_GROUP, "기본");
        fixture.links.seed(20L, TARGET_GROUP, 0);
        return fixture;
    }

    private static final class Fixture {

        private final FakeProductOptionGroupLinkRepository links = new FakeProductOptionGroupLinkRepository();
        private final Map<Long, Product> products = new LinkedHashMap<>();
        private final Map<Long, ProductOptionGroup> groups = new LinkedHashMap<>();
        private final FakeProductOptionRepository options = new FakeProductOptionRepository();
        private final List<ProductOptionGroupMergeHistory> histories = new ArrayList<>();
        private final ProductOptionGroupMergeService service;

        private Fixture() {
            ProductOptionGroupLinkService linkService =
                new ProductOptionGroupLinkService(links, new StubProductRepository(products));
            this.service = new ProductOptionGroupMergeService(
                new StubOptionGroupRepository(groups),
                options,
                links,
                linkService,
                new RecordingMergeHistoryRepository(histories)
            );
        }

        private void merge(Long... targetIds) {
            service.merge(
                MY_SHOP,
                ProductOptionGroupId.of(BASE_GROUP),
                Stream.of(targetIds).map(ProductOptionGroupId::of).toList(),
                ProductOptionGroupMergeEntryType.RECOMMENDED,
                ACTOR
            );
        }

        private void addProduct(Long id, ShopId shopId) {
            products.put(id, Product.reconstitute(
                id, shopId, null, "메뉴" + id, null, 1000, null, null, 0,
                false, null, false, null, true, 0,
                false, false, null, false, null, null, null, null, null, null
            ));
        }

        private void addGroup(Long id, String name, Integer minSelect) {
            addGroup(id, name, minSelect, ProductOptionGroupType.NORMAL);
        }

        private void addGroup(
            Long id,
            String name,
            Integer minSelect,
            ProductOptionGroupType groupType
        ) {
            groups.put(id, ProductOptionGroup.reconstitute(
                id, ProductId.of(10L), name, null, false, false, minSelect, 1, 0, true, groupType
            ));
        }

        private void addOption(Long id, Long groupId, String name) {
            options.seed(ProductOption.reconstitute(
                id, ProductOptionGroupId.of(groupId), name, 0, 0, false, null, true, null, null
            ));
        }

        private List<Long> optionIdsOf() {
            return options.findAllByOptionGroupId(ProductOptionGroupId.of(BASE_GROUP)).stream()
                .map(ProductOption::getId)
                .toList();
        }
    }

    /** 옵션 write 포트의 인메모리 fake — 같은 인스턴스를 돌려주므로 상태 변경이 그대로 보인다. */
    private static final class FakeProductOptionRepository implements ProductOptionRepository {

        private final Map<Long, ProductOption> options = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong(1000L);

        private void seed(ProductOption option) {
            options.put(option.getId(), option);
        }

        @Override
        public ProductOption save(ProductOption option) {
            if (option.getId() == null) {
                Long id = sequence.getAndIncrement();
                ProductOption persisted = ProductOption.reconstitute(
                    id, option.getOptionGroupId(), option.getName(), option.getAdditionalPrice(),
                    option.getSort(), option.isSoldOut(), option.getSoldOutUntil(), option.isVisible(),
                    option.getCupCount(), option.getPersonalCupDiscountAmount()
                );
                options.put(id, persisted);
                return persisted;
            }
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
        public List<ProductOption> findAllSoldOutExpiredBefore(java.time.LocalDateTime baseTime) {
            return List.of();
        }
    }

    /** {@code findById}·{@code save}만 쓰는 최소 스텁. */
    private static final class StubOptionGroupRepository implements ProductOptionGroupRepository {

        private final Map<Long, ProductOptionGroup> groups;

        private StubOptionGroupRepository(Map<Long, ProductOptionGroup> groups) {
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

    /** 이력 append를 그대로 모아두는 fake. */
    private static final class RecordingMergeHistoryRepository
        implements ProductOptionGroupMergeHistoryRepository {

        private final List<ProductOptionGroupMergeHistory> histories;

        private RecordingMergeHistoryRepository(List<ProductOptionGroupMergeHistory> histories) {
            this.histories = histories;
        }

        @Override
        public ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history) {
            histories.add(history);
            return history;
        }

        @Override
        public List<ProductOptionGroupMergeHistory> findAllByMergedOptionGroupId(
            ProductOptionGroupId mergedOptionGroupId
        ) {
            return histories.stream()
                .filter(history -> history.getMergedOptionGroupId().equals(mergedOptionGroupId))
                .toList();
        }

        @Override
        public List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId) {
            return histories.stream()
                .filter(history -> history.getShopId().equals(shopId))
                .toList();
        }
    }
}
