package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductBbqPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.testsupport.product.service.FakeProductShopLinkPersistencePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRegistrationServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final Long PRODUCT_ID = 7L;
    private static final Long OPTION_GROUP_ID = 11L;

    @Test
    @DisplayName("상품을 저장한다")
    void createProduct_saves() {
        Fixture fixture = new Fixture(null);

        Product created = fixture.service.createProduct(
            SHOP_ID, ProductCategoryId.of(2L), "황금올리브치킨", "바삭한 치킨",
            20000, null, null, null, 0, true, 1, false, true, 0, false, null, false
        );

        assertThat(created.getName()).isEqualTo("황금올리브치킨");
        assertThat(fixture.productPersistencePort.saved).hasSize(1);
    }

    @Test
    @DisplayName("상품 정보를 변경한 뒤 명시적으로 저장한다")
    void updateProduct_savesExplicitly() {
        Fixture fixture = new Fixture(product());

        fixture.service.updateProduct(
            ProductId.of(PRODUCT_ID), ProductCategoryId.of(3L), "변경된 이름", "변경된 설명",
            25000, 20000, null, false, 2, false, true, 1
        );

        assertThat(fixture.productPersistencePort.saved).hasSize(1);
        assertThat(fixture.productPersistencePort.saved.getFirst().getName()).isEqualTo("변경된 이름");
    }

    @Test
    @DisplayName("품절 처리 시 상태를 전이하고 명시적으로 저장한다")
    void markSoldOut_transitionsAndSaves() {
        Fixture fixture = new Fixture(product());

        fixture.service.markSoldOut(ProductId.of(PRODUCT_ID));

        assertThat(fixture.productPersistencePort.saved).hasSize(1);
        assertThat(fixture.productPersistencePort.saved.getFirst().isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("비활성화 시 노출을 끄고 명시적으로 저장한다")
    void deactivateProduct_transitionsAndSaves() {
        Fixture fixture = new Fixture(product());

        fixture.service.deactivateProduct(ProductId.of(PRODUCT_ID));

        assertThat(fixture.productPersistencePort.saved).hasSize(1);
        assertThat(fixture.productPersistencePort.saved.getFirst().isVisible()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 상품을 수정하면 예외를 던진다")
    void updateProduct_throwsWhenMissing() {
        Fixture fixture = new Fixture(null);

        assertThatThrownBy(() -> fixture.service.updateProduct(
            ProductId.of(PRODUCT_ID), ProductCategoryId.of(3L), "이름", null,
            1000, null, null, false, null, false, true, 0
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("BBQ 옵션 동기화 완료를 표시한 뒤 명시적으로 저장한다")
    void markBbqOptionsSynced_savesExplicitly() {
        Fixture fixture = new Fixture(product());
        fixture.bbqPersistencePort.stored = ProductBbq.reconstitute(5L, ProductId.of(PRODUCT_ID), BbqMenuId.of(100L), BbqCategoryId.of(200L), false);

        fixture.service.markBbqOptionsSynced(ProductId.of(PRODUCT_ID));

        assertThat(fixture.bbqPersistencePort.saved).hasSize(1);
        assertThat(fixture.bbqPersistencePort.saved.getFirst().isOptionsSynced()).isTrue();
    }

    @Test
    @DisplayName("BBQ 매핑이 없으면 동기화 표시가 예외를 던진다")
    void markBbqOptionsSynced_throwsWhenMappingMissing() {
        Fixture fixture = new Fixture(product());

        assertThatThrownBy(() -> fixture.service.markBbqOptionsSynced(ProductId.of(PRODUCT_ID)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리·이미지·옵션그룹·옵션·BBQ 매핑을 각 포트로 저장한다")
    void saveChildAggregates_delegateToEachPort() {
        Fixture fixture = new Fixture(null);

        fixture.service.createProductCategory(SHOP_ID, "치킨", null, 0, true);
        fixture.service.saveProductImage(ProductId.of(PRODUCT_ID), UploadedFileId.of(99L), 0, true);
        fixture.service.saveProductOptionGroup(
            ProductId.of(PRODUCT_ID), "맛 선택", null, true, false, 1, 1, 0, true,
            ProductOptionGroupType.NORMAL
        );
        fixture.service.saveProductOption(ProductOptionGroupId.of(11L), "순살", 2000, 0, false, true, null, null);
        fixture.service.saveProductBbq(ProductId.of(PRODUCT_ID), BbqMenuId.of(100L), BbqCategoryId.of(200L), false);

        assertThat(fixture.categoryPersistencePort.saved).hasSize(1);
        assertThat(fixture.imagePersistencePort.saved).hasSize(1);
        assertThat(fixture.optionGroupPersistencePort.saved).hasSize(1);
        assertThat(fixture.optionPersistencePort.saved).hasSize(1);
        assertThat(fixture.bbqPersistencePort.saved).hasSize(1);
    }

    private Product product() {
        return Product.reconstitute(
            PRODUCT_ID, SHOP_ID, ProductCategoryId.of(2L), "황금올리브치킨", "바삭한 치킨",
            20000, null, null, 0, true, 1, false, null, true, 0, false,
            false, null, false, null, null, null, null, null, null
        );
    }

    private static final class Fixture {

        private final ProductPersistencePortStub productPersistencePort;
        private final ProductCategoryPersistencePortStub categoryPersistencePort = new ProductCategoryPersistencePortStub();
        private final ProductOptionGroupPersistencePortStub optionGroupPersistencePort = new ProductOptionGroupPersistencePortStub();
        private final ProductOptionPersistencePortStub optionPersistencePort = new ProductOptionPersistencePortStub();
        private final ProductImagePersistencePortStub imagePersistencePort = new ProductImagePersistencePortStub();
        private final ProductBbqPersistencePortStub bbqPersistencePort = new ProductBbqPersistencePortStub();
        private final ProductRegistrationService service;

        private Fixture(Product existing) {
            this.productPersistencePort = new ProductPersistencePortStub(existing);
            ProductOptionGroupLinkPersistencePortStub optionGroupLinkPersistencePort = new ProductOptionGroupLinkPersistencePortStub();

            this.service = new ProductRegistrationService(
                productPersistencePort,
                categoryPersistencePort,
                optionGroupPersistencePort,
                optionPersistencePort,
                imagePersistencePort,
                bbqPersistencePort,
                optionGroupLinkPersistencePort,
                new FakeProductShopLinkPersistencePort()
            );
        }
    }

    private static final class ProductPersistencePortStub implements ProductPersistencePort {

        private final Product existing;
        private final List<Product> saved = new ArrayList<>();

        private ProductPersistencePortStub(Product existing) {
            this.existing = existing;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(existing);
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
            return List.of();
        }

        @Override
        public Optional<Product> findByIdIncludingDeleted(ProductId id) {
            return Optional.ofNullable(existing);
        }

        @Override
        public boolean existsByShopIdAndName(ShopId shopId, String name) {
            return existing != null && existing.getName().equals(name);
        }

        @Override
        public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
            return existing != null
                && !existing.getId().equals(excludedId.value())
                && existing.getName().equals(name);
        }

        @Override
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product save(Product product) {
            saved.add(product);
            if (product.getId() != null) {
                return product;
            }
            return Product.reconstitute(
                PRODUCT_ID,
                product.getShopId(),
                product.getProductCategoryId(),
                product.getName(),
                product.getDescription(),
                product.getOriginalPrice(),
                ProductDiscountInfo.of(product.getDiscountPrice(), product.getDiscountRate()),
                product.getRating(),
                product.getReviewCount(),
                product.isRepresentative(),
                product.getSpiciness(),
                product.isSoldOut(),
                product.getSoldOutUntil(),
                product.isVisible(),
                product.getSort(),
                product.isRatingExcluded(),
                false,
                product.getComposition(),
                product.isSingleServing(),
                product.getExposureStartDate(),
                product.getExposureEndDate(),
                product.getVegetarianType(),
                null,
                null,
                null
            );
        }
    }

    private static final class ProductCategoryPersistencePortStub implements ProductCategoryPersistencePort {

        private final List<ProductCategory> saved = new ArrayList<>();

        @Override
        public Optional<ProductCategory> findById(ProductCategoryId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId) {
            return List.of();
        }

        @Override
        public ProductCategory save(ProductCategory productCategory) {
            saved.add(productCategory);
            return productCategory;
        }

        @Override
        public List<ProductCategory> findAllByShopId(ShopId shopId) {
            return List.copyOf(saved);
        }

        @Override
        public void delete(ProductCategory productCategory) {
            saved.remove(productCategory);
        }
    }

    private static final class ProductOptionGroupPersistencePortStub implements ProductOptionGroupPersistencePort {

        private final List<ProductOptionGroup> saved = new ArrayList<>();

        @Override
        public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
            return List.of();
        }

        @Override
        public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
            saved.add(productOptionGroup);
            if (productOptionGroup.getId() != null) {
                return productOptionGroup;
            }
            return ProductOptionGroup.reconstitute(
                OPTION_GROUP_ID,
                productOptionGroup.getProductId(),
                productOptionGroup.getName(),
                productOptionGroup.getDescription(),
                productOptionGroup.isRequired(),
                productOptionGroup.isMultipleSelect(),
                productOptionGroup.getMinSelect(),
                productOptionGroup.getMaxSelect(),
                productOptionGroup.getSort(),
                productOptionGroup.isVisible(),
                ProductOptionGroupType.NORMAL
            );
        }
    }

    private static final class ProductOptionPersistencePortStub implements ProductOptionPersistencePort {

        private final List<ProductOption> saved = new ArrayList<>();

        @Override
        public Optional<ProductOption> findById(ProductOptionId id) {
            return Optional.empty();
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
            return List.of();
        }

        @Override
        public ProductOption save(ProductOption productOption) {
            saved.add(productOption);
            return productOption;
        }
    }

    private static final class ProductImagePersistencePortStub implements ProductImagePersistencePort {

        private final List<ProductImage> saved = new ArrayList<>();

        @Override
        public UploadedFileId findRepresentativeImageFileId(ProductId productId) {
            return null;
        }

        @Override
        public ProductImage save(ProductImage productImage) {
            saved.add(productImage);
            return productImage;
        }

        @Override
        public Optional<ProductImage> findById(Long id) {
            return saved.stream().filter(image -> id.equals(image.getId())).findFirst();
        }

        @Override
        public List<ProductImage> findAllByProductId(ProductId productId) {
            return saved.stream()
                .filter(image -> image.getProductId().equals(productId))
                .toList();
        }

        @Override
        public void delete(ProductImage productImage) {
            saved.remove(productImage);
        }
    }

    private static final class ProductBbqPersistencePortStub implements ProductBbqPersistencePort {

        private final List<ProductBbq> saved = new ArrayList<>();
        private ProductBbq stored;

        @Override
        public Optional<ProductBbq> findByProductId(ProductId productId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public ProductBbq save(ProductBbq productBbq) {
            saved.add(productBbq);
            return productBbq;
        }
    }

    private static final class ProductOptionGroupLinkPersistencePortStub implements ProductOptionGroupLinkPersistencePort {

        private final List<ProductOptionGroupLink> saved = new ArrayList<>();

        @Override
        public ProductOptionGroupLink save(ProductOptionGroupLink link) {
            saved.add(link);
            return link;
        }

        @Override
        public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
            ProductId productId,
            ProductOptionGroupId optionGroupId
        ) {
            return saved.stream()
                .filter(link -> link.getProductId().equals(productId)
                    && link.getOptionGroupId().equals(optionGroupId))
                .findFirst();
        }

        @Override
        public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
            return saved.stream()
                .filter(link -> link.getProductId().equals(productId))
                .toList();
        }

        @Override
        public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
            return saved.stream()
                .filter(link -> link.getOptionGroupId().equals(optionGroupId))
                .toList();
        }

        @Override
        public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
            return saved.stream()
                .filter(link -> optionGroupIds.contains(link.getOptionGroupId()))
                .toList();
        }

        @Override
        public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
            return findByProductIdAndOptionGroupId(productId, optionGroupId).isPresent();
        }

        @Override
        public void delete(ProductOptionGroupLink link) {
            saved.remove(link);
        }
    }
}
