package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.repository.ShopMenuCollectionImageRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴모음컷 등록·검수·배치 워크플로의 불변식 봉인 테스트.
 *
 * <p>이 서비스의 규칙은 전부 <b>행 하나만 보고는 판정할 수 없는 집합 차원</b>이라, 애그리거트 단위
 * 테스트로는 한 줄도 검증되지 않는다. 그래서 네 가지를 각각 못 박는다.
 *
 * <ul>
 *   <li><b>정원(최대 6개)은 상태를 가리지 않는다</b> — 대기·반려 건도 슬롯을 차지한다. 승인분만 세면
 *       반려된 것을 지우지 않고 계속 올려 검수 큐를 한 가게로 채울 수 있다.</li>
 *   <li><b>하한(최소 1개)은 승인분만 센다</b> — 이 테스트의 최고 가치 지점이다. 전체 건수로 세면
 *       "승인 1 + 반려 1"인 가게에서 승인분 삭제가 통과해(전체가 2건이므로) 손님이 보는 자리가
 *       빈 채로 노출된다. 반려·대기 행은 노출되지 않으므로 하한을 지탱하지 못한다.</li>
 *   <li><b>배치(순서 변경·삭제)는 검수 대상이 아니다</b> — 승인 없이 즉시 반영되고, 반대로
 *       순서를 바꿨다는 이유로 대기 중인 이미지가 노출되어서도 안 된다.</li>
 *   <li><b>순서 변경은 replace-all</b> — 부분·초과·미지의 id 목록은 전부 거절한다. 부분 목록을 받아주면
 *       낡은 화면에서 보낸 요청이 빠진 이미지를 목록 끝으로 밀어내는데, 점주는 순서만 바꿨다고 믿는다.</li>
 * </ul>
 *
 * <p>가게 소유가 아닌 id는 존재를 알리지 않고 {@code SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND}로 합쳐
 * IDOR을 막는다 — 이 경로도 함께 봉인한다.
 */
class ShopMenuCollectionImageServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final ShopId OTHER_SHOP_ID = ShopId.of(2L);

    // ── 등록 ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("등록하면 PENDING 행이 맨 뒤(sort = 현재 개수)에 붙고 손님에게는 아직 노출되지 않는다")
    void registerAppendsPendingImageAtTheEnd() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));

        Long imageId = fixture.service.register(SHOP_ID, UploadedFileId.of(700L));

        ShopMenuCollectionImage registered = fixture.imageRepository.require(imageId);
        assertThat(registered.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(registered.getSort()).isEqualTo(2);
        assertThat(registered.getImageFileId()).isEqualTo(UploadedFileId.of(700L));
    }

    @Test
    @DisplayName("첫 등록은 sort 0을 받는다 — 빈 가게에서도 순서 부여가 성립한다")
    void firstRegistrationGetsSortZero() {
        Fixture fixture = fixture();

        Long imageId = fixture.service.register(SHOP_ID, UploadedFileId.of(700L));

        assertThat(fixture.imageRepository.require(imageId).getSort()).isZero();
    }

    @Test
    @DisplayName("없는 가게에 등록하면 SHOP_NOT_FOUND — 유령 가게에 이미지가 붙지 않는다")
    void registerOnMissingShopIsRejected() {
        Fixture fixture = fixture();

        assertThatThrownBy(() -> fixture.service.register(ShopId.of(99L), UploadedFileId.of(700L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_NOT_FOUND);
    }

    @Test
    @DisplayName("6개가 차 있으면 7번째 등록은 SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED로 거부된다")
    void registerBeyondSixIsRejected() {
        Fixture fixture = fixture(
            approved(10L, 0), approved(11L, 1), approved(12L, 2),
            approved(13L, 3), approved(14L, 4), approved(15L, 5)
        );

        assertThatThrownBy(() -> fixture.service.register(SHOP_ID, UploadedFileId.of(700L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("정원은 상태를 가리지 않는다 — 승인 1 + 대기 3 + 반려 2로도 6개가 차 등록이 거부된다")
    void pendingAndRejectedImagesOccupySlots() {
        Fixture fixture = fixture(
            approved(10L, 0),
            pending(11L, 1), pending(12L, 2), pending(13L, 3),
            rejected(14L, 4), rejected(15L, 5)
        );

        assertThatThrownBy(() -> fixture.service.register(SHOP_ID, UploadedFileId.of(700L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("정확히 5개까지는 통과한다 — 상한이 6개로 열려 있다(off-by-one 방지)")
    void registerAtFiveSucceeds() {
        Fixture fixture = fixture(
            approved(10L, 0), pending(11L, 1), rejected(12L, 2),
            approved(13L, 3), pending(14L, 4)
        );

        Long imageId = fixture.service.register(SHOP_ID, UploadedFileId.of(700L));

        assertThat(fixture.imageRepository.require(imageId).getSort()).isEqualTo(5);
    }

    // ── 삭제 (최소 1개 유지) ────────────────────────────────────────────────────

    @Test
    @DisplayName("승인 1 + 반려 1에서 승인분 삭제는 거부된다 — 전체 건수로 세면 통과해 손님 화면이 빈다")
    void deletingTheOnlyApprovedImageIsRejectedEvenWhenARejectedRowExists() {
        Fixture fixture = fixture(approved(10L, 0), rejected(11L, 1));

        assertThatThrownBy(() -> fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(10L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE);
        assertThat(fixture.imageRepository.idsOf(SHOP_ID)).containsExactly(10L, 11L);
    }

    @Test
    @DisplayName("승인 1 + 반려 1에서 반려분 삭제는 통과한다 — 노출 건수가 줄지 않는다")
    void deletingARejectedImageSucceedsWhileOneApprovedRemains() {
        Fixture fixture = fixture(approved(10L, 0), rejected(11L, 1));

        fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(11L));

        assertThat(fixture.imageRepository.idsOf(SHOP_ID)).containsExactly(10L);
    }

    @Test
    @DisplayName("대기분 삭제도 통과한다 — 대기 중인 것은 아직 노출되지 않아 하한을 지탱하지 않는다")
    void deletingAPendingImageSucceedsWhileOneApprovedRemains() {
        Fixture fixture = fixture(approved(10L, 0), pending(11L, 1));

        fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(11L));

        assertThat(fixture.imageRepository.idsOf(SHOP_ID)).containsExactly(10L);
    }

    @Test
    @DisplayName("승인 2개 중 하나를 지우는 것은 통과한다 — 승인분이 1개 남는다")
    void deletingOneOfTwoApprovedImagesSucceeds() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));

        fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(10L));

        assertThat(fixture.imageRepository.idsOf(SHOP_ID)).containsExactly(11L);
    }

    @Test
    @DisplayName("승인분이 하나뿐이면 그것을 지우려는 요청은 거부된다 — 0개가 되면 첫 화면이 빈다")
    void deletingTheLastApprovedImageIsRejected() {
        Fixture fixture = fixture(approved(10L, 0));

        assertThatThrownBy(() -> fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(10L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE);
    }

    @Test
    @DisplayName("삭제 후 남은 것의 sort가 0..N-1로 다시 매겨진다 — 구멍이 남으면 다음 등록이 쓰인 순서를 받는다")
    void deleteRenumbersRemainingSortWithoutGaps() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1), pending(12L, 2), approved(13L, 3));

        fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(11L));

        assertThat(fixture.imageRepository.sortsOf())
            .containsExactly(entry(10L, 0), entry(12L, 1), entry(13L, 2));
    }

    @Test
    @DisplayName("남의 가게 이미지 삭제는 SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND로 합쳐 존재를 숨긴다")
    void deletingAnotherShopsImageIsNotFound() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));
        fixture.imageRepository.put(otherShopApproved());

        assertThatThrownBy(() -> fixture.service.delete(SHOP_ID, ShopMenuCollectionImageId.of(20L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND);
        assertThat(fixture.imageRepository.idsOf(OTHER_SHOP_ID)).containsExactly(20L);
    }

    // ── 순서 변경 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 목록을 순서만 바꿔 보내면 sort가 보낸 순서대로 0..N-1로 다시 매겨진다")
    void reorderRenumbersToRequestedOrder() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1), approved(12L, 2));

        fixture.service.reorder(SHOP_ID, List.of(12L, 10L, 11L));

        assertThat(fixture.imageRepository.sortsOf())
            .containsExactly(entry(12L, 0), entry(10L, 1), entry(11L, 2));
    }

    @Test
    @DisplayName("순서를 바꿔도 status는 그대로다 — 배치는 검수 대상이 아니므로 대기 이미지가 노출되지 않는다")
    void reorderNeverChangesStatus() {
        Fixture fixture = fixture(approved(10L, 0), pending(11L, 1), rejected(12L, 2));

        fixture.service.reorder(SHOP_ID, List.of(11L, 12L, 10L));

        assertThat(fixture.imageRepository.require(10L).getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(fixture.imageRepository.require(11L).getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(fixture.imageRepository.require(12L).getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(fixture.imageRepository.require(12L).getRejectReason()).isEqualTo("사진이 어둡습니다.");
    }

    @Test
    @DisplayName("부분 목록은 ORDER_TARGET_MISMATCH로 거부된다 — 빠진 이미지가 조용히 뒤로 밀리지 않는다")
    void reorderWithSubsetIsRejected() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1), approved(12L, 2));

        assertThatThrownBy(() -> fixture.service.reorder(SHOP_ID, List.of(12L, 10L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        assertThat(fixture.imageRepository.sortsOf())
            .containsExactly(entry(10L, 0), entry(11L, 1), entry(12L, 2));
    }

    @Test
    @DisplayName("현재 목록에 없는 id가 섞인 초과 목록도 ORDER_TARGET_MISMATCH로 거부된다")
    void reorderWithSupersetIsRejected() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));

        assertThatThrownBy(() -> fixture.service.reorder(SHOP_ID, List.of(10L, 11L, 99L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
    }

    @Test
    @DisplayName("개수는 맞지만 모르는 id가 섞이면 거부된다 — 개수만 세는 검증으로는 통과해버린다")
    void reorderWithUnknownIdOfSameSizeIsRejected() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));

        assertThatThrownBy(() -> fixture.service.reorder(SHOP_ID, List.of(10L, 99L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        assertThat(fixture.imageRepository.sortsOf())
            .containsExactly(entry(10L, 0), entry(11L, 1));
    }

    @Test
    @DisplayName("남의 가게 이미지 id로 순서를 바꾸려는 요청도 거부된다 — 남의 행이 내 목록으로 끌려오지 않는다")
    void reorderWithAnotherShopsImageIdIsRejected() {
        Fixture fixture = fixture(approved(10L, 0), approved(11L, 1));
        fixture.imageRepository.put(otherShopApproved());

        assertThatThrownBy(() -> fixture.service.reorder(SHOP_ID, List.of(10L, 20L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue(
                "errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        assertThat(fixture.imageRepository.require(20L).getSort()).isZero();
    }

    // ── 검수 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("승인하면 APPROVED가 되고 sort는 건드리지 않는다 — 승인만으로 첫 화면 이미지가 바뀌지 않는다")
    void approveFlipsStatusOnly() {
        Fixture fixture = fixture(approved(10L, 0), pending(11L, 1));

        fixture.service.approve(ShopMenuCollectionImageId.of(11L));

        ShopMenuCollectionImage approved = fixture.imageRepository.require(11L);
        assertThat(approved.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(approved.getRejectReason()).isNull();
        assertThat(approved.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("반려하면 REJECTED가 되고 사유가 남는다 — 점주가 무엇을 고쳐 다시 올릴지 알아야 한다")
    void rejectStoresReason() {
        Fixture fixture = fixture(approved(10L, 0), pending(11L, 1));

        fixture.service.reject(ShopMenuCollectionImageId.of(11L), "메뉴판 글씨가 흐립니다.");

        ShopMenuCollectionImage rejected = fixture.imageRepository.require(11L);
        assertThat(rejected.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(rejected.getRejectReason()).isEqualTo("메뉴판 글씨가 흐립니다.");
    }

    @Test
    @DisplayName("이미 승인된 건을 다시 승인하면 NOT_PENDING(409) — 검수는 한 번만 통과한다")
    void approvingAnApprovedImageIsRejected() {
        Fixture fixture = fixture(approved(10L, 0));

        assertThatThrownBy(() -> fixture.service.approve(ShopMenuCollectionImageId.of(10L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING);
    }

    @Test
    @DisplayName("이미 반려된 건을 승인하면 NOT_PENDING — 반려분이 재등록 없이 되살아나지 않는다")
    void approvingARejectedImageIsRejected() {
        Fixture fixture = fixture(approved(10L, 0), rejected(11L, 1));

        assertThatThrownBy(() -> fixture.service.approve(ShopMenuCollectionImageId.of(11L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING);
        assertThat(fixture.imageRepository.require(11L).getStatus()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @Test
    @DisplayName("이미 승인된 건을 반려하면 NOT_PENDING — 노출 중인 이미지가 사후 반려로 사라지지 않는다")
    void rejectingAnApprovedImageIsRejected() {
        Fixture fixture = fixture(approved(10L, 0));

        assertThatThrownBy(() ->
            fixture.service.reject(ShopMenuCollectionImageId.of(10L), "다시 보니 흐립니다."))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING);
        assertThat(fixture.imageRepository.require(10L).getRejectReason()).isNull();
    }

    @Test
    @DisplayName("없는 이미지를 검수하면 SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND(404)")
    void reviewingMissingImageIsNotFound() {
        Fixture fixture = fixture(approved(10L, 0));

        assertThatThrownBy(() -> fixture.service.approve(ShopMenuCollectionImageId.of(99L)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND);
    }

    // ── 픽스처 ────────────────────────────────────────────────────────────────

    private static ShopMenuCollectionImage approved(Long id, int sort) {
        return image(id, SHOP_ID, sort, ApprovalStatus.APPROVED, null);
    }

    private static ShopMenuCollectionImage pending(Long id, int sort) {
        return image(id, SHOP_ID, sort, ApprovalStatus.PENDING, null);
    }

    private static ShopMenuCollectionImage rejected(Long id, int sort) {
        return image(id, SHOP_ID, sort, ApprovalStatus.REJECTED, "사진이 어둡습니다.");
    }

    private static ShopMenuCollectionImage otherShopApproved() {
        return image(20L, OTHER_SHOP_ID, 0, ApprovalStatus.APPROVED, null);
    }

    private static ShopMenuCollectionImage image(
        Long id,
        ShopId shopId,
        int sort,
        ApprovalStatus status,
        String rejectReason
    ) {
        return ShopMenuCollectionImage.reconstitute(
            id, shopId, UploadedFileId.of(500L + id), sort, status, rejectReason, null, null
        );
    }

    private static Map.Entry<Long, Integer> entry(Long imageId, int sort) {
        return Map.entry(imageId, sort);
    }

    private static Fixture fixture(ShopMenuCollectionImage... images) {
        FakeShopMenuCollectionImageRepository imageRepository =
            new FakeShopMenuCollectionImageRepository(List.of(images));
        return new Fixture(
            new ShopMenuCollectionImageService(imageRepository, new FakeShopRepository()),
            imageRepository
        );
    }

    private record Fixture(
        ShopMenuCollectionImageService service,
        FakeShopMenuCollectionImageRepository imageRepository
    ) {
    }

    /**
     * 메뉴모음컷 write 포트 fake. 신규 저장 시 식별자를 부여하고, 기존 행은 <b>같은 인스턴스를 그대로</b>
     * 보관한다 — 서비스가 전이 메서드로 바꾼 상태·순서가 저장소에 반영되는 실제 동작과 같아진다.
     *
     * <p>{@code findAllByShopId}는 write 포트 계약대로 <b>상태 무관 전량을 sort 오름차순</b>으로 준다.
     * 여기서 상태로 좁히면 정원·하한 판정이 조용히 다른 전제 위에서 통과한다.
     */
    private static final class FakeShopMenuCollectionImageRepository
        implements ShopMenuCollectionImageRepository {

        private final Map<Long, ShopMenuCollectionImage> images = new LinkedHashMap<>();
        private long sequence = 900L;

        private FakeShopMenuCollectionImageRepository(List<ShopMenuCollectionImage> images) {
            images.forEach(this::put);
        }

        void put(ShopMenuCollectionImage image) {
            this.images.put(image.getId(), image);
        }

        ShopMenuCollectionImage require(Long id) {
            ShopMenuCollectionImage image = images.get(id);
            if (image == null) {
                throw new AssertionError("저장되지 않은 메뉴모음컷: " + id);
            }
            return image;
        }

        List<Long> idsOf(ShopId shopId) {
            return imagesOf(shopId).stream().map(ShopMenuCollectionImage::getId).toList();
        }

        /** id → sort 쌍을 sort 오름차순으로 준다. 재부여 결과를 순서째로 대조하기 위한 것이다. */
        List<Map.Entry<Long, Integer>> sortsOf() {
            return imagesOf(SHOP_ID).stream()
                .map(image -> Map.entry(image.getId(), image.getSort()))
                .toList();
        }

        private List<ShopMenuCollectionImage> imagesOf(ShopId shopId) {
            List<ShopMenuCollectionImage> found = new ArrayList<>();
            for (ShopMenuCollectionImage image : images.values()) {
                if (image.getShopId().equals(shopId)) {
                    found.add(image);
                }
            }
            found.sort(Comparator.comparingInt(ShopMenuCollectionImage::getSort));
            return found;
        }

        @Override
        public ShopMenuCollectionImage save(ShopMenuCollectionImage image) {
            if (image.getId() != null) {
                images.put(image.getId(), image);
                return image;
            }
            ShopMenuCollectionImage persisted = ShopMenuCollectionImage.reconstitute(
                ++sequence,
                image.getShopId(),
                image.getImageFileId(),
                image.getSort(),
                image.getStatus(),
                image.getRejectReason(),
                null,
                null
            );
            images.put(persisted.getId(), persisted);
            return persisted;
        }

        @Override
        public Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id) {
            return Optional.ofNullable(images.get(id.value()));
        }

        @Override
        public List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId) {
            return imagesOf(shopId);
        }

        @Override
        public void delete(ShopMenuCollectionImage image) {
            images.remove(image.getId());
        }
    }

    /** 가게 존재 검증만 실제로 동작하는 fake. 등록 경로가 가게를 찾을 수 있어야 한다. */
    private static final class FakeShopRepository implements ShopRepository {

        private final Map<Long, Shop> shops = new LinkedHashMap<>();

        private FakeShopRepository() {
            shops.put(SHOP_ID.value(), shop(SHOP_ID));
            shops.put(OTHER_SHOP_ID.value(), shop(OTHER_SHOP_ID));
        }

        private static Shop shop(ShopId shopId) {
            return Shop.reconstitute(
                shopId.value(), null, null, "맛있는 분식 " + shopId.value(),
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, false, false, false, 10000, false, false, null, null
            );
        }

        @Override
        public Optional<Shop> findById(ShopId shopId) {
            return Optional.ofNullable(shops.get(shopId.value()));
        }

        @Override
        public Optional<Shop> findVisibleById(ShopId shopId) {
            return findById(shopId);
        }

        @Override
        public Shop save(Shop shop) {
            throw new UnsupportedOperationException();
        }
    }
}
