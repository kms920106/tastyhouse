package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 사장님 추천(대표 메뉴) 승인 워크플로의 불변식 봉인 테스트.
 *
 * <p>세 제약(최대 6개 · 이미지 필수 · 최소 1개 유지)이 각각 어느 코드로 거부되는지를 고정한다 —
 * 특히 <b>최소 1개 유지가 기존 {@code PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE}를 재사용</b>하는 것이
 * 이 테스트가 지키는 핵심이다. 새 코드로 갈라지면 같은 불변식에 프론트가 두 갈래를 분기해야 하고,
 * 일괄 숨김 경로와 하한이 어긋난다.
 */
class ProductRepresentativeApprovalServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Test
    @DisplayName("지정을 신청하면 PENDING 요청이 생기고 Product는 아직 켜지지 않는다")
    void requestCreatesPendingWithoutFlippingProduct() {
        Product product = product(10L, false);
        Fixture fixture = fixture(List.of(product), List.of(10L));

        List<Long> requestIds = fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(requestIds).hasSize(1);
        assertThat(product.isRepresentative()).isFalse();
        assertThat(fixture.requestRepository.saved).hasSize(1);
        assertThat(fixture.requestRepository.saved.getFirst().getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("이미지가 없는 메뉴는 PRODUCT_REPRESENTATIVE_IMAGE_REQUIRED로 거부된다")
    void requestWithoutImageIsRejected() {
        Fixture fixture = fixture(List.of(product(10L, false)), List.of());

        assertThatThrownBy(() -> fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(10L))))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_REPRESENTATIVE_IMAGE_REQUIRED);
    }

    @Test
    @DisplayName("현재 대표 4개 + 대기 1건이면 2개 추가 신청은 6개를 넘어 거부된다")
    void pendingRequestsCountTowardTheLimit() {
        Fixture fixture = fixture(
            List.of(product(10L, false), product(11L, false)),
            List.of(10L, 11L)
        );
        fixture.productRepository.visibleRepresentativeCount = 4L;
        fixture.requestRepository.pendingCount = 1L;

        assertThatThrownBy(() -> fixture.service.requestRepresentative(
            SHOP_ID, List.of(ProductId.of(10L), ProductId.of(11L))))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("대기 건수를 세지 않으면 통과했을 조합(4 + 대기 1 + 1)이 정확히 6개로 통과한다")
    void limitIsInclusiveOfSix() {
        Fixture fixture = fixture(List.of(product(10L, false)), List.of(10L));
        fixture.productRepository.visibleRepresentativeCount = 4L;
        fixture.requestRepository.pendingCount = 1L;

        List<Long> requestIds = fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(requestIds).hasSize(1);
    }

    @Test
    @DisplayName("이미 대표인 메뉴는 건너뛴다(멱등) — 개수에도 다시 세지 않는다")
    void alreadyRepresentativeIsSkipped() {
        Fixture fixture = fixture(List.of(product(10L, true)), List.of(10L));
        fixture.productRepository.visibleRepresentativeCount = 6L;

        List<Long> requestIds = fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(10L)));

        assertThat(requestIds).isEmpty();
    }

    @Test
    @DisplayName("승인하면 Product.representative가 켜지고 요청은 APPROVED가 된다")
    void approveFlipsProductColumn() {
        Product product = product(10L, false);
        Fixture fixture = fixture(List.of(product), List.of(10L));
        ProductRepresentativeRequest request = fixture.givenPendingRequest();

        fixture.service.approve(ProductRepresentativeRequestId.of(1L));

        assertThat(product.isRepresentative()).isTrue();
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("승인 시점에도 개수 제한을 다시 검증한다 — 대기 중에 가게 상태가 달라질 수 있다")
    void approveRevalidatesLimit() {
        Fixture fixture = fixture(List.of(product(10L, false)), List.of(10L));
        fixture.givenPendingRequest();
        fixture.productRepository.visibleRepresentativeCount = 6L;

        assertThatThrownBy(() -> fixture.service.approve(ProductRepresentativeRequestId.of(1L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("없는 요청을 승인하면 PRODUCT_REPRESENTATIVE_REQUEST_NOT_FOUND(404)")
    void approveMissingRequestIsNotFound() {
        Fixture fixture = fixture(List.of(product(10L, false)), List.of(10L));

        assertThatThrownBy(() -> fixture.service.approve(ProductRepresentativeRequestId.of(99L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("반려하면 사유가 남고 Product는 켜지지 않는다")
    void rejectKeepsReasonAndDoesNotFlip() {
        Product product = product(10L, false);
        Fixture fixture = fixture(List.of(product), List.of(10L));
        ProductRepresentativeRequest request = fixture.givenPendingRequest();

        fixture.service.reject(ProductRepresentativeRequestId.of(1L), "사진이 어둡습니다.");

        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(request.getRejectReason()).isEqualTo("사진이 어둡습니다.");
        assertThat(product.isRepresentative()).isFalse();
    }

    @Test
    @DisplayName("해제는 승인 없이 즉시 반영된다")
    void clearTakesEffectImmediately() {
        Product product = product(10L, true);
        Fixture fixture = fixture(List.of(product), List.of(10L));
        fixture.productRepository.visibleRepresentativeCount = 3L;

        fixture.service.clearRepresentative(SHOP_ID, ProductId.of(10L));

        assertThat(product.isRepresentative()).isFalse();
        assertThat(fixture.requestRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("마지막 1개 해제는 기존 PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE(400)로 거부된다")
    void clearLastRepresentativeReusesExistingCode() {
        Product product = product(10L, true);
        Fixture fixture = fixture(List.of(product), List.of(10L));
        fixture.productRepository.visibleRepresentativeCount = 1L;

        assertThatThrownBy(() -> fixture.service.clearRepresentative(SHOP_ID, ProductId.of(10L)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        assertThat(product.isRepresentative()).isTrue();
    }

    @Test
    @DisplayName("이미 해제 상태면 최소 1개 검증도 건너뛴다(멱등)")
    void clearIsIdempotent() {
        Product product = product(10L, false);
        Fixture fixture = fixture(List.of(product), List.of(10L));
        fixture.productRepository.visibleRepresentativeCount = 0L;

        fixture.service.clearRepresentative(SHOP_ID, ProductId.of(10L));

        assertThat(product.isRepresentative()).isFalse();
    }

    @Test
    @DisplayName("남의 가게 메뉴는 PRODUCT_NOT_FOUND로 합쳐 존재 여부를 숨긴다")
    void otherShopProductIsNotFound() {
        Fixture fixture = fixture(List.of(), List.of());

        assertThatThrownBy(() -> fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(10L))))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    // ── 픽스처 ────────────────────────────────────────────────────────────────

    private static Product product(Long id, boolean representative) {
        return Product.reconstitute(
            id, SHOP_ID, ProductCategoryId.of(2L), "메뉴" + id, "설명", 10000,
            null, null, 0, representative, null, false, null, true, 1, false,
            false, null, false, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("6개를 한 번에 신청한 뒤 순차 승인하면 6건 모두 승인된다 — 승인 판정이 자기 요청을 이중 계상하지 않는다")
    void approvingAFullBatchOfSixSucceedsForEveryRequest() {
        List<Product> products = new ArrayList<>();
        List<Long> withImage = new ArrayList<>();
        List<ProductId> targets = new ArrayList<>();
        for (long id = 10L; id < 16L; id++) {
            products.add(product(id, false));
            withImage.add(id);
            targets.add(ProductId.of(id));
        }
        Fixture fixture = fixture(products, withImage);
        fixture.productRepository.visibleRepresentativeCount = 0L;

        List<Long> requestIds = fixture.service.requestRepresentative(SHOP_ID, targets);
        assertThat(requestIds).hasSize(6);

        // 승인이 진행될수록 켜진 개수가 늘고 대기 건수는 줄어든다 — 실제 운영 순서를 그대로 재현한다.
        for (int index = 0; index < requestIds.size(); index++) {
            fixture.productRepository.visibleRepresentativeCount = index;
            fixture.requestRepository.pendingCount = 6L - index;
            fixture.service.approve(ProductRepresentativeRequestId.of(requestIds.get(index)));
        }

        assertThat(products).allMatch(Product::isRepresentative);
    }

    @Test
    @DisplayName("숨긴 대표 메뉴도 최대 6개 상한에 포함된다 — 숨김으로 상한을 우회할 수 없다")
    void hiddenRepresentativesStillCountTowardTheLimit() {
        Fixture fixture = fixture(List.of(product(20L, false)), List.of(20L));
        // 노출 3개 + 숨김 3개 = 총 6개. 노출분만 세면 3개로 보여 통과해버린다.
        fixture.productRepository.visibleRepresentativeCount = 3L;
        fixture.productRepository.totalRepresentativeCount = 6L;

        assertThatThrownBy(() -> fixture.service.requestRepresentative(SHOP_ID, List.of(ProductId.of(20L))))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
    }

    private static Fixture fixture(List<Product> products, List<Long> productIdsWithImage) {
        FakeProductRepository productRepository = new FakeProductRepository(products);
        FakeRepresentativeRequestRepository requestRepository = new FakeRepresentativeRequestRepository();
        FakeProductImageRepository imageRepository = new FakeProductImageRepository(productIdsWithImage);
        return new Fixture(
            new ProductRepresentativeApprovalService(requestRepository, productRepository, imageRepository),
            productRepository,
            requestRepository
        );
    }

    private record Fixture(
        ProductRepresentativeApprovalService service,
        FakeProductRepository productRepository,
        FakeRepresentativeRequestRepository requestRepository
    ) {

        ProductRepresentativeRequest givenPendingRequest() {
            Long requestId = 1L;
            ProductRepresentativeRequest request = ProductRepresentativeRequest.reconstitute(
                requestId, ProductId.of(10L), SHOP_ID, ApprovalStatus.PENDING, null, null, null);
            requestRepository.byId.put(requestId, request);
            return request;
        }
    }

    /**
     * 소유 가게 필터와 대표 메뉴 카운트만 실제로 동작하는 fake. 나머지는 이 테스트가 호출하지 않으므로
     * {@code UnsupportedOperationException}을 던진다 — 조용히 빈 값을 돌려주면 잘못된 전제 위에서
     * 테스트가 통과한다.
     */
    private static final class FakeProductRepository implements ProductRepository {

        private final Map<Long, Product> products = new LinkedHashMap<>();
        private long visibleRepresentativeCount;
        private Long totalRepresentativeCount;

        private FakeProductRepository(List<Product> products) {
            products.forEach(product -> this.products.put(product.getId(), product));
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(products.get(id.value()));
        }

        @Override
        public Product save(Product product) {
            return product;
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
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            return visibleRepresentativeCount;
        }

        @Override
        public long countRepresentativeByShopId(ShopId shopId) {
            // 명시하지 않으면 노출분과 같다고 본다(숨김 없는 가게). 숨김을 섞는 테스트만 따로 지정한다.
            return totalRepresentativeCount != null ? totalRepresentativeCount : visibleRepresentativeCount;
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

    private static final class FakeRepresentativeRequestRepository
        implements ProductRepresentativeRequestRepository {

        private final Map<Long, ProductRepresentativeRequest> byId = new LinkedHashMap<>();
        private final List<ProductRepresentativeRequest> saved = new ArrayList<>();
        private long pendingCount;
        private long sequence = 100L;

        @Override
        public ProductRepresentativeRequest save(ProductRepresentativeRequest request) {
            if (request.getId() == null) {
                ProductRepresentativeRequest persisted = ProductRepresentativeRequest.reconstitute(
                    ++sequence,
                    request.getProductId(),
                    request.getShopId(),
                    request.getStatus(),
                    request.getRejectReason(),
                    null,
                    null
                );
                byId.put(persisted.getId(), persisted);
                saved.add(persisted);
                return persisted;
            }
            saved.add(request);
            return request;
        }

        @Override
        public Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id) {
            return Optional.ofNullable(byId.get(id.value()));
        }

        @Override
        public List<ProductRepresentativeRequest> findAllByProductId(ProductId productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
            return byId.values().stream()
                .anyMatch(request -> request.getProductId().equals(productId)
                    && request.getStatus() == status);
        }

        @Override
        public long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status) {
            return status == ApprovalStatus.PENDING ? pendingCount : 0L;
        }
    }

    private static final class FakeProductImageRepository implements ProductImageRepository {

        private final List<Long> productIdsWithImage;

        private FakeProductImageRepository(List<Long> productIdsWithImage) {
            this.productIdsWithImage = productIdsWithImage;
        }

        @Override
        public UploadedFileId findRepresentativeImageFileId(ProductId productId) {
            return productIdsWithImage.contains(productId.value()) ? UploadedFileId.of(7L) : null;
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(ProductImage productImage) {
            throw new UnsupportedOperationException();
        }
    }
}
