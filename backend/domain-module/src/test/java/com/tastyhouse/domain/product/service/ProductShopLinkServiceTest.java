package com.tastyhouse.domain.product.service;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 ↔ 가게 연결 불변식의 순수 단위 테스트.
 *
 * <p><b>이 설계의 안전장치가 "링크가 1개인 메뉴는 동작이 완전히 그대로"라는 것</b>이므로, 최소 1개
 * 유지·메뉴그룹 소속 대조·본인 소유 가게 제한 세 규칙을 못 박는다. 하나라도 뚫리면 메뉴가 어느
 * 메뉴판에도 없는 유령이 되거나, 남의 가게 메뉴판을 조작할 수 있게 된다.
 */
class ProductShopLinkServiceTest {

    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final ShopId OWNER_SHOP = ShopId.of(10L);
    private static final ShopId OTHER_OWNED_SHOP = ShopId.of(20L);
    private static final ShopId FOREIGN_SHOP = ShopId.of(99L);
    private static final Long OWNER_CATEGORY = 100L;
    private static final Long OTHER_CATEGORY = 200L;
    private static final Long FOREIGN_CATEGORY = 900L;
    private static final Set<Long> OWNED = Set.of(OWNER_SHOP.value(), OTHER_OWNED_SHOP.value());

    private FakeShopLinkProductRepository productRepository;
    private FakeProductShopLinkRepository linkRepository;
    private ProductShopLinkService service;

    @BeforeEach
    void setUp() {
        productRepository = new FakeShopLinkProductRepository();
        linkRepository = new FakeProductShopLinkRepository();
        FakeShopLinkProductCategoryRepository categoryRepository = new FakeShopLinkProductCategoryRepository();
        service = new ProductShopLinkService(productRepository, linkRepository, categoryRepository);

        productRepository.given(visibleProduct());
        // 메뉴판이 비지 않도록 넉넉히 둔다 — 노출 개수 제약은 별도 테스트에서 따로 검증한다.
        productRepository.givenVisibleCount(OWNER_SHOP, 5L);
        productRepository.givenVisibleCount(OTHER_OWNED_SHOP, 5L);

        categoryRepository.given(OWNER_CATEGORY, OWNER_SHOP);
        categoryRepository.given(OTHER_CATEGORY, OTHER_OWNED_SHOP);
        categoryRepository.given(FOREIGN_CATEGORY, FOREIGN_SHOP);
    }

    private static Product visibleProduct() {
        return Product.reconstitute(
            PRODUCT_ID.value(), OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), "후라이드", null,
            10000, null, null, 0, false, null, false, null, true, 0, false, false,
            null, false, null, null, null, null, null, null
        );
    }

    private static ProductShopLinkSpec spec(ShopId shopId, Long categoryId) {
        return ProductShopLinkSpec.of(shopId.value(), categoryId);
    }

    @Nested
    @DisplayName("연결 전체 교체")
    class ReplaceLinks {

        @Test
        @DisplayName("빈 목록은 거절한다 — 링크가 0개면 어느 메뉴판에도 없는 유령 메뉴가 된다")
        void emptyLinks_rejected() {
            assertThatThrownBy(() -> service.replaceLinks(PRODUCT_ID, List.of(), OWNED))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        @Test
        @DisplayName("본인 소유가 아닌 가게는 거절한다")
        void foreignShop_rejected() {
            assertThatThrownBy(() -> service.replaceLinks(
                PRODUCT_ID, List.of(spec(FOREIGN_SHOP, FOREIGN_CATEGORY)), OWNED
            ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
        }

        @Test
        @DisplayName("메뉴그룹을 지정하지 않으면 거절한다")
        void missingCategory_rejected() {
            assertThatThrownBy(() -> service.replaceLinks(
                PRODUCT_ID, List.of(spec(OWNER_SHOP, null)), OWNED
            ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_REQUIRED);
        }

        @Test
        @DisplayName("그 가게의 메뉴그룹이 아니면 거절한다 — 남의 가게 메뉴판 구조를 들여다볼 수 없어야 한다")
        void categoryOfAnotherShop_rejected() {
            assertThatThrownBy(() -> service.replaceLinks(
                PRODUCT_ID, List.of(spec(OWNER_SHOP, OTHER_CATEGORY)), OWNED
            ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("새 가게를 추가하면 링크가 생긴다")
        void addsNewLink() {
            service.replaceLinks(
                PRODUCT_ID,
                List.of(spec(OWNER_SHOP, OWNER_CATEGORY), spec(OTHER_OWNED_SHOP, OTHER_CATEGORY)),
                OWNED
            );

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID))
                .extracting(link -> link.getShopId().value())
                .containsExactlyInAnyOrder(OWNER_SHOP.value(), OTHER_OWNED_SHOP.value());
        }

        @Test
        @DisplayName("목록에 없는 가게는 연결 해제된다")
        void removesMissingLink() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);
            linkRepository.given(PRODUCT_ID, OTHER_OWNED_SHOP, ProductCategoryId.of(OTHER_CATEGORY), 0);

            service.replaceLinks(PRODUCT_ID, List.of(spec(OWNER_SHOP, OWNER_CATEGORY)), OWNED);

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID))
                .extracting(link -> link.getShopId().value())
                .containsExactly(OWNER_SHOP.value());
        }

        @Test
        @DisplayName("기존 연결의 표시 순서는 유지된다 — 다른 가게의 변경이 그 메뉴판 배열을 흔들면 안 된다")
        void keepsExistingSort() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 7);

            service.replaceLinks(
                PRODUCT_ID,
                List.of(spec(OWNER_SHOP, OWNER_CATEGORY), spec(OTHER_OWNED_SHOP, OTHER_CATEGORY)),
                OWNED
            );

            ProductShopLink kept = linkRepository.findByProductIdAndShopId(PRODUCT_ID, OWNER_SHOP).orElseThrow();
            assertThat(kept.getSort()).isEqualTo(7);
        }

        @Test
        @DisplayName("해제로 그 가게 메뉴판의 노출 메뉴가 0개가 되면 거절한다")
        void lastVisibleInShop_cannotUnlink() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);
            linkRepository.given(PRODUCT_ID, OTHER_OWNED_SHOP, ProductCategoryId.of(OTHER_CATEGORY), 0);
            productRepository.givenVisibleCount(OTHER_OWNED_SHOP, 1L);

            assertThatThrownBy(() -> service.replaceLinks(
                PRODUCT_ID, List.of(spec(OWNER_SHOP, OWNER_CATEGORY)), OWNED
            ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        }
    }

    @Nested
    @DisplayName("가게 기준 불러오기·제외")
    class LinkAndUnlink {

        @Test
        @DisplayName("이미 연결된 가게는 거절한다 — 조용히 통과시키면 메뉴그룹이 이전 값 그대로여서 결과가 어긋난다")
        void alreadyLinked_rejected() {
            linkRepository.given(PRODUCT_ID, OTHER_OWNED_SHOP, ProductCategoryId.of(OTHER_CATEGORY), 0);

            assertThatThrownBy(() -> service.linkToShop(PRODUCT_ID, OTHER_OWNED_SHOP, OTHER_CATEGORY))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_ALREADY_LINKED);
        }

        @Test
        @DisplayName("새 연결은 대상 가게 메뉴판 끝 순서로 붙는다")
        void appendsToEndOfTargetShop() {
            linkRepository.given(ProductId.of(2L), OTHER_OWNED_SHOP, ProductCategoryId.of(OTHER_CATEGORY), 3);

            service.linkToShop(PRODUCT_ID, OTHER_OWNED_SHOP, OTHER_CATEGORY);

            ProductShopLink created =
                linkRepository.findByProductIdAndShopId(PRODUCT_ID, OTHER_OWNED_SHOP).orElseThrow();
            assertThat(created.getSort()).isEqualTo(4);
        }

        @Test
        @DisplayName("마지막 연결은 해제할 수 없다")
        void lastLink_cannotUnlink() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);

            assertThatThrownBy(() -> service.unlinkFromShop(PRODUCT_ID, OWNER_SHOP))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        @Test
        @DisplayName("연결이 2개 이상이면 제외할 수 있고 메뉴 자체는 남는다")
        void unlinksWhenMultipleLinks() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);
            linkRepository.given(PRODUCT_ID, OTHER_OWNED_SHOP, ProductCategoryId.of(OTHER_CATEGORY), 0);

            service.unlinkFromShop(PRODUCT_ID, OTHER_OWNED_SHOP);

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID))
                .extracting(link -> link.getShopId().value())
                .containsExactly(OWNER_SHOP.value());
        }

        @Test
        @DisplayName("연결되지 않은 가게에서 제외하면 404다")
        void notLinked_notFound() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);

            assertThatThrownBy(() -> service.unlinkFromShop(PRODUCT_ID, OTHER_OWNED_SHOP))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 등록 시 추가 연결")
    class CreateInitialLinks {

        /**
         * 원본 가게 링크는 {@code ProductRegistrationService}가 저장과 같은 자리에서 이미 만든다.
         * 등록 직후 상태를 재현하기 위해 미리 심어 둔다.
         */
        @BeforeEach
        void givenOwnerLinkCreatedAtRegistration() {
            linkRepository.given(PRODUCT_ID, OWNER_SHOP, ProductCategoryId.of(OWNER_CATEGORY), 0);
        }

        @Test
        @DisplayName("links가 비면 아무것도 하지 않는다 — 원본 가게 단일 연결 그대로다")
        void emptyLinks_keepsOwnerLinkOnly() {
            service.createInitialLinks(PRODUCT_ID, List.of(), OWNED);

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID))
                .singleElement()
                .satisfies(link -> assertThat(link.getShopId()).isEqualTo(OWNER_SHOP));
        }

        @Test
        @DisplayName("null이어도 원본 가게 단일 연결 그대로다")
        void nullLinks_keepsOwnerLinkOnly() {
            assertThatCode(() -> service.createInitialLinks(PRODUCT_ID, null, OWNED))
                .doesNotThrowAnyException();

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID)).hasSize(1);
        }

        @Test
        @DisplayName("원본 가게가 목록에 함께 실려 와도 중복 저장하지 않는다")
        void ownerShopInLinks_notDuplicated() {
            service.createInitialLinks(
                PRODUCT_ID,
                List.of(spec(OWNER_SHOP, OWNER_CATEGORY), spec(OTHER_OWNED_SHOP, OTHER_CATEGORY)),
                OWNED
            );

            assertThat(linkRepository.findAllByProductId(PRODUCT_ID))
                .extracting(link -> link.getShopId().value())
                .containsExactlyInAnyOrder(OWNER_SHOP.value(), OTHER_OWNED_SHOP.value());
        }

        @Test
        @DisplayName("본인 소유가 아닌 가게가 섞이면 거절한다")
        void foreignShopInLinks_rejected() {
            assertThatThrownBy(() -> service.createInitialLinks(
                PRODUCT_ID, List.of(spec(FOREIGN_SHOP, FOREIGN_CATEGORY)), OWNED
            ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
        }
    }
}
