package com.tastyhouse.core.domain.product.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.product.domain.event.ProductCreatedEvent;
import com.tastyhouse.core.domain.product.domain.event.ProductDeactivatedEvent;
import com.tastyhouse.core.domain.product.domain.event.ProductSoldOutChangedEvent;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductCategoryId;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionGroupId;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.shared.event.DomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 상품 등록·구성 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트와 이벤트 발행 포트를 손으로 만든 스텁으로
 * 대체해 검증한다. 특히 <b>도메인 모델 변경 후 명시적 save 호출</b>(더티 체킹 없음)과 이벤트 발행을 확인한다.
 */
class ProductRegistrationServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long PRODUCT_ID = 7L;

    @Test
    @DisplayName("상품을 저장하고 생성 이벤트를 발행한다")
    void createProduct_savesAndPublishesEvent() {
        Fixture fixture = new Fixture(null);

        Product created = fixture.service.createProduct(
            SHOP_ID, 2L, "황금올리브치킨", "바삭한 치킨",
            20000, null, null, null, 0, true, 1, false, true, 0
        );

        assertThat(created.getName()).isEqualTo("황금올리브치킨");
        assertThat(fixture.productRepository.saved).hasSize(1);
        assertThat(fixture.publisher.published).hasSize(1);
        assertThat(fixture.publisher.published.getFirst()).isInstanceOf(ProductCreatedEvent.class);
    }

    @Test
    @DisplayName("상품 정보를 변경한 뒤 명시적으로 저장한다")
    void updateProduct_savesExplicitly() {
        Fixture fixture = new Fixture(product());

        fixture.service.updateProduct(
            ProductId.of(PRODUCT_ID), 3L, "변경된 이름", "변경된 설명",
            25000, 20000, null, false, 2, false, true, 1
        );

        assertThat(fixture.productRepository.saved).hasSize(1);
        assertThat(fixture.productRepository.saved.getFirst().getName()).isEqualTo("변경된 이름");
        assertThat(fixture.publisher.published).isEmpty();
    }

    @Test
    @DisplayName("품절 처리 시 상태를 전이하고 저장한 뒤 품절 이벤트를 발행한다")
    void markSoldOut_transitionsSavesAndPublishes() {
        Fixture fixture = new Fixture(product());

        fixture.service.markSoldOut(ProductId.of(PRODUCT_ID));

        assertThat(fixture.productRepository.saved).hasSize(1);
        assertThat(fixture.productRepository.saved.getFirst().isSoldOut()).isTrue();
        assertThat(fixture.publisher.published.getFirst()).isInstanceOf(ProductSoldOutChangedEvent.class);
    }

    @Test
    @DisplayName("비활성화 시 노출을 끄고 저장한 뒤 비활성 이벤트를 발행한다")
    void deactivateProduct_transitionsSavesAndPublishes() {
        Fixture fixture = new Fixture(product());

        fixture.service.deactivateProduct(ProductId.of(PRODUCT_ID));

        assertThat(fixture.productRepository.saved).hasSize(1);
        assertThat(fixture.productRepository.saved.getFirst().isVisible()).isFalse();
        assertThat(fixture.publisher.published.getFirst()).isInstanceOf(ProductDeactivatedEvent.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 수정하면 예외를 던진다")
    void updateProduct_throwsWhenMissing() {
        Fixture fixture = new Fixture(null);

        assertThatThrownBy(() -> fixture.service.updateProduct(
            ProductId.of(PRODUCT_ID), 3L, "이름", null,
            1000, null, null, false, null, false, true, 0
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("BBQ 옵션 동기화 완료를 표시한 뒤 명시적으로 저장한다")
    void markBbqOptionsSynced_savesExplicitly() {
        Fixture fixture = new Fixture(product());
        fixture.bbqRepository.stored = ProductBbq.reconstitute(5L, PRODUCT_ID, 100L, 200L, false);

        fixture.service.markBbqOptionsSynced(ProductId.of(PRODUCT_ID));

        assertThat(fixture.bbqRepository.saved).hasSize(1);
        assertThat(fixture.bbqRepository.saved.getFirst().isOptionsSynced()).isTrue();
    }

    @Test
    @DisplayName("BBQ 매핑이 없으면 동기화 표시가 예외를 던진다")
    void markBbqOptionsSynced_throwsWhenMappingMissing() {
        Fixture fixture = new Fixture(product());

        assertThatThrownBy(() -> fixture.service.markBbqOptionsSynced(ProductId.of(PRODUCT_ID)))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리·이미지·옵션그룹·옵션·BBQ 매핑을 각 포트로 저장한다")
    void saveChildAggregates_delegateToEachPort() {
        Fixture fixture = new Fixture(null);

        fixture.service.createProductCategory(SHOP_ID, "치킨", 0, true);
        fixture.service.saveProductImage(PRODUCT_ID, 99L, 0, true);
        fixture.service.saveProductOptionGroup(PRODUCT_ID, "맛 선택", null, true, false, 1, 1, 0, true);
        fixture.service.saveProductOption(11L, "순살", 2000, 0, false, true);
        fixture.service.saveProductBbq(PRODUCT_ID, 100L, 200L, false);

        assertThat(fixture.categoryRepository.saved).hasSize(1);
        assertThat(fixture.imageRepository.saved).hasSize(1);
        assertThat(fixture.optionGroupRepository.saved).hasSize(1);
        assertThat(fixture.optionRepository.saved).hasSize(1);
        assertThat(fixture.bbqRepository.saved).hasSize(1);
    }

    private Product product() {
        return Product.reconstitute(
            PRODUCT_ID, SHOP_ID, 2L, "황금올리브치킨", "바삭한 치킨",
            20000, null, null, 0, true, 1, false, true, 0, null, null
        );
    }

    /**
     * 도메인 서비스와 그 협력 스텁을 한 번에 조립하는 테스트 픽스처.
     */
    private static final class Fixture {

        private final ProductRepositoryStub productRepository;
        private final ProductCategoryRepositoryStub categoryRepository = new ProductCategoryRepositoryStub();
        private final ProductOptionGroupRepositoryStub optionGroupRepository = new ProductOptionGroupRepositoryStub();
        private final ProductOptionRepositoryStub optionRepository = new ProductOptionRepositoryStub();
        private final ProductImageRepositoryStub imageRepository = new ProductImageRepositoryStub();
        private final ProductBbqRepositoryStub bbqRepository = new ProductBbqRepositoryStub();
        private final DomainEventPublisherStub publisher = new DomainEventPublisherStub();
        private final ProductRegistrationService service;

        private Fixture(Product existing) {
            this.productRepository = new ProductRepositoryStub(existing);
            this.service = new ProductRegistrationService(
                productRepository,
                categoryRepository,
                optionGroupRepository,
                optionRepository,
                imageRepository,
                bbqRepository,
                publisher
            );
        }
    }

    private static final class ProductRepositoryStub implements ProductRepository {

        private final Product existing;
        private final List<Product> saved = new ArrayList<>();

        private ProductRepositoryStub(Product existing) {
            this.existing = existing;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(existing);
        }

        @Override
        public Product save(Product product) {
            saved.add(product);
            return product;
        }
    }

    private static final class ProductCategoryRepositoryStub implements ProductCategoryRepository {

        private final List<ProductCategory> saved = new ArrayList<>();

        @Override
        public Optional<ProductCategory> findById(ProductCategoryId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId) {
            return List.of();
        }

        @Override
        public ProductCategory save(ProductCategory productCategory) {
            saved.add(productCategory);
            return productCategory;
        }
    }

    private static final class ProductOptionGroupRepositoryStub implements ProductOptionGroupRepository {

        private final List<ProductOptionGroup> saved = new ArrayList<>();

        @Override
        public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
            return Optional.empty();
        }

        @Override
        public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
            saved.add(productOptionGroup);
            return productOptionGroup;
        }
    }

    private static final class ProductOptionRepositoryStub implements ProductOptionRepository {

        private final List<ProductOption> saved = new ArrayList<>();

        @Override
        public Optional<ProductOption> findById(ProductOptionId id) {
            return Optional.empty();
        }

        @Override
        public ProductOption save(ProductOption productOption) {
            saved.add(productOption);
            return productOption;
        }
    }

    private static final class ProductImageRepositoryStub implements ProductImageRepository {

        private final List<ProductImage> saved = new ArrayList<>();

        @Override
        public String findRepresentativeImageFilePath(Long productId) {
            return null;
        }

        @Override
        public ProductImage save(ProductImage productImage) {
            saved.add(productImage);
            return productImage;
        }
    }

    private static final class ProductBbqRepositoryStub implements ProductBbqRepository {

        private final List<ProductBbq> saved = new ArrayList<>();
        private ProductBbq stored;

        @Override
        public Optional<ProductBbq> findByProductId(Long productId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public ProductBbq save(ProductBbq productBbq) {
            saved.add(productBbq);
            return productBbq;
        }
    }

    private static final class DomainEventPublisherStub implements DomainEventPublisher {

        private final List<Object> published = new ArrayList<>();

        @Override
        public void publish(Object event) {
            published.add(event);
        }
    }
}
