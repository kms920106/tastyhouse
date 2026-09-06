package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 가격(가격명 + 채널별 가격) 등록·수정의 단일 소유자.
 *
 * <p><b>전체 교체(PUT) 의미론이다</b> — 요청에 담긴 목록이 그 메뉴의 가격 전체가 되고, 담기지 않은 기존
 * 행은 삭제된다(순서 변경 API와 같은 의미론). 부분 갱신을 두지 않는 이유는 가격 행의 정렬·가격명 중복
 * 같은 <b>컬렉션 단위 불변식</b>이 전체를 함께 봐야만 판정되기 때문이다.
 *
 * <p><b>{@code PRODUCT.original_price} 동기화가 이 서비스의 가장 중요한 책임이다.</b> 기존 코드 수십
 * 곳(주문·검색·오늘의할인·목록)이 그 컬럼을 읽고 있어 걷어낼 수 없으므로, {@code sort=0} 행의 배달가를
 * 그 컬럼에 옮겨 적어 <b>가격 행이 1개인 메뉴의 동작을 완전히 그대로</b> 유지한다 — 이것이 이 설계의
 * 안전장치다.
 *
 * <p><b>인증 OFF 판정도 여기서 동기 수행한다.</b> 가격이 바뀌어 배달가 &gt; 매장가가 되면 그 자리에서
 * 가게 인증을 내린다 — 배치로 미루면 그 사이 손님이 잘못된 '매장과 같은 가격' 뱃지를 본다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 api 모듈의 command
 * 서비스가 선언한다.
 */
public class ProductPriceService {

    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public ProductPriceService(
        ProductPriceRepository productPriceRepository,
        ProductRepository productRepository,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    /**
     * 메뉴의 가격 목록을 조회한다. {@code sort} 오름차순이다.
     *
     * <p>가격 행이 하나도 없으면 <b>실패시키지 않고 빈 목록을 돌려준다</b> — 이관 이후 신규 메뉴는
     * 항상 행을 갖지만, 조회가 500이 되면 목록 화면 전체가 죽는다. 저장 경로가 1개 이상을 강제하므로
     * (아래 {@link #replacePrices}) 빈 상태는 조회에서 막을 대상이 아니다.
     */
    public List<ProductPrice> findPrices(ShopId shopId, ProductId productId) {
        loadOwnedProduct(shopId, productId);
        return productPriceRepository.findAllByProductId(productId);
    }

    /**
     * 메뉴의 가격 목록을 <b>전체 교체</b>한다.
     *
     * <p>검증 순서에 의도가 있다 — 소유권 → 할인 진행 → 컬렉션 불변식 → 인증 게이트 순이다. 소유권을
     * 가장 앞에 두어 남의 가게 메뉴의 존재 여부가 다른 에러코드로 새어 나가지 않게 하고, 인증 게이트를
     * 가장 뒤에 두어 "가격명이 중복인데 인증도 안 됐다"는 상황에서 점주가 먼저 고쳐야 할 것(가격명)을
     * 먼저 알려준다.
     *
     * @param specs 새 가격 목록. {@code id}가 있으면 기존 행 갱신, 없으면 신규 추가다
     * @return 저장된 가격 행 목록({@code sort} 오름차순)
     */
    public List<ProductPrice> replacePrices(
        ShopId shopId,
        ProductId productId,
        List<ProductPriceSpec> specs,
        LocalDateTime now
    ) {
        Product product = loadOwnedProduct(shopId, productId);

        requireNoDiscountInProgress(product);
        validateSpecs(specs);
        requireVerifiedIfStoreOrPickupPriceGiven(shopId, specs);

        List<ProductPrice> existing = productPriceRepository.findAllByProductId(productId);
        List<ProductPrice> saved = applySpecs(productId, specs, existing, now);

        syncOriginalPrice(product, saved);
        refreshStorePriceVerification(shopId);

        return saved;
    }

    /**
     * 요청 목록을 기존 행에 반영한다 — 갱신·추가·삭제를 한 번에 수행한다.
     *
     * <p>삭제를 <b>마지막에</b> 하는 이유는, 먼저 지우면 갱신 대상 행을 잃어 "존재하지 않는 가격"으로
     * 실패하기 때문이다.
     */
    private List<ProductPrice> applySpecs(
        ProductId productId,
        List<ProductPriceSpec> specs,
        List<ProductPrice> existing,
        LocalDateTime now
    ) {
        Set<Long> keptIds = new LinkedHashSet<>();
        List<ProductPrice> saved = new ArrayList<>();

        for (ProductPriceSpec spec : specs) {
            if (spec.id() == null) {
                saved.add(productPriceRepository.save(ProductPrice.of(
                    productId,
                    spec.priceName(),
                    spec.deliveryPrice(),
                    spec.storePrice(),
                    spec.pickupPrice(),
                    spec.sort(),
                    now
                )));
                continue;
            }

            ProductPrice target = existing.stream()
                .filter(price -> spec.id().equals(price.getId()))
                .findFirst()
                // 다른 메뉴의 가격 id를 실어 보낸 경우도 여기서 걸린다 — existing이 이 메뉴의 행만 담기
                // 때문이다. 남의 행을 이 메뉴로 끌어오는 우회가 구조적으로 막힌다.
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND,
                    ErrorCode.PRODUCT_PRICE_NOT_FOUND.getDefaultMessage() + ": " + spec.id()));

            target.change(
                spec.priceName(),
                spec.deliveryPrice(),
                spec.storePrice(),
                spec.pickupPrice(),
                spec.sort(),
                now
            );
            saved.add(productPriceRepository.save(target));
            keptIds.add(spec.id());
        }

        List<ProductPriceId> removed = existing.stream()
            .filter(price -> !keptIds.contains(price.getId()))
            .map(ProductPrice::getProductPriceId)
            .toList();
        if (!removed.isEmpty()) {
            productPriceRepository.deleteAllByIdIn(removed);
        }

        return saved.stream()
            .sorted(Comparator.comparingInt(price -> orZero(price.getSort())))
            .toList();
    }

    /**
     * 컬렉션 단위 불변식을 검증한다 — 행 하나만 봐서는 판정할 수 없는 것들이다.
     *
     * <ul>
     *   <li>가격 행은 <b>1개 이상</b>({@code PRODUCT_PRICE_EMPTY})</li>
     *   <li>2개 이상이면 가격명 <b>필수</b>({@code PRODUCT_PRICE_NAME_REQUIRED}) — 손님이 무엇을 고르는지
     *       구별할 수 없으면 여러 가격을 두는 의미가 없다</li>
     *   <li>가격명 <b>중복 불가</b>({@code PRODUCT_PRICE_NAME_DUPLICATED}) — DB 유니크 제약과 짝을 이룬다.
     *       DB에만 맡기면 제약 위반이 500으로 새어 나간다</li>
     * </ul>
     *
     * <p>개별 가격의 음수 검증은 {@code ProductPrice}가 소유한다(한 행의 값만으로 판정 가능).
     */
    private static void validateSpecs(List<ProductPriceSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_EMPTY);
        }

        if (specs.size() == 1) {
            return;
        }

        Set<String> names = new HashSet<>();
        for (ProductPriceSpec spec : specs) {
            String priceName = spec.priceName();
            if (priceName == null || priceName.isBlank()) {
                throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_REQUIRED);
            }
            if (!names.add(priceName)) {
                throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_DUPLICATED,
                    ErrorCode.PRODUCT_PRICE_NAME_DUPLICATED.getDefaultMessage() + ": " + priceName);
            }
        }
    }

    /**
     * 매장가·픽업가가 하나라도 담겼으면 <b>인증된 가게여야 한다.</b>
     *
     * <p>배달가는 상시 변경 가능하므로 이 게이트에 걸리지 않는다 — 인증되지 않은 가게도 배달가는
     * 자유롭게 바꿀 수 있어야 한다.
     */
    private void requireVerifiedIfStoreOrPickupPriceGiven(ShopId shopId, List<ProductPriceSpec> specs) {
        boolean given = specs.stream()
            .anyMatch(spec -> spec.storePrice() != null || spec.pickupPrice() != null);
        if (!given) {
            return;
        }
        if (!storePriceVerificationPort.isStorePriceVerified(shopId.value())) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_STORE_NOT_VERIFIED);
        }
    }

    /**
     * 할인 중인 메뉴는 가격을 바꿀 수 없다.
     *
     * <p><b>이 저장소에는 할인 스케줄링(대기/진행 상태)이 없으므로 "현재 할인가가 설정돼 있는가"로
     * 판정한다.</b> {@code PRODUCT.discount_price}가 유일한 할인 표현이며 기간·상태 컬럼이 존재하지 않는다.
     * 할인 스케줄이 도입되면 이 술어만 교체하면 된다 — 그것이 이 판정을 별도 메서드로 둔 이유다.
     */
    private static void requireNoDiscountInProgress(Product product) {
        if (product.getDiscountPrice() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_DISCOUNT_IN_PROGRESS);
        }
    }

    /**
     * {@code sort=0} 행(기본 가격)의 배달가를 {@code PRODUCT.original_price}에 동기화한다.
     *
     * <p><b>이 동기화를 빠뜨리면 주문 금액 대조가 어긋나 주문이 전부 실패한다</b> — 기존 주문 경로가
     * 여전히 그 컬럼을 읽기 때문이다.
     *
     * <p>{@code changeDetails}가 아니라 가격 전용 전이({@code Product#syncOriginalPrice})를 쓰는 이유는
     * 가격 저장이 메뉴명·설명 같은 다른 필드를 건드려서는 안 되기 때문이다.
     */
    private void syncOriginalPrice(Product product, List<ProductPrice> saved) {
        if (saved.isEmpty()) {
            return;
        }
        Integer basePrice = saved.getFirst().getDeliveryPrice();
        if (basePrice == null) {
            return;
        }
        product.syncOriginalPrice(basePrice);
        productRepository.save(product);
    }

    /**
     * 가게 전체 가격을 다시 훑어 매장가격 인증 상태를 재판정한다(재인증 필요 판정).
     *
     * <p><b>한 메뉴라도 배달가 &gt; 매장가면 가게 인증을 내린다.</b> 뱃지는 가게 단위로 노출되므로
     * 한 메뉴만 어긋나도 "매장과 같은 가격"이라는 표시는 거짓이 된다.
     *
     * <p>인증을 다시 켜지는 않는다 — 켜는 것은 관리자 승인의 권한이다. 여기서 자동으로 켜면 반려된
     * 가게가 가격만 맞춰 인증을 얻는 우회가 생긴다.
     */
    private void refreshStorePriceVerification(ShopId shopId) {
        if (!storePriceVerificationPort.isStorePriceVerified(shopId.value())) {
            return;
        }
        List<ProductPrice> violated = productPriceRepository.findAllByShopId(shopId).stream()
            .filter(ProductPrice::isDeliveryPriceHigherThanStorePrice)
            .toList();
        if (violated.isEmpty()) {
            return;
        }

        // 정리를 먼저, 상태 전이를 나중에 — 인증만 꺼진 채 옛 매장가가 남는 반쪽 상태를 만들지 않는다.
        for (ProductPrice price : violated) {
            price.clearStoreAndPickupPrice();
            productPriceRepository.save(price);
        }
        storePriceVerificationPort.clearStorePriceVerification(shopId.value());
    }

    /**
     * 대상 메뉴가 정말 그 가게 것인지 확인하며 로드한다.
     *
     * <p>"메뉴 없음"과 "남의 가게 메뉴"를 같은 {@code PRODUCT_NOT_FOUND}(404)로 합친다 — 코드가 갈리면
     * 남의 가게 메뉴의 존재 여부가 새어 나가 식별자 열거에 쓰인다
     * ({@code ProductRepresentativeApprovalService}와 같은 판단).
     */
    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
