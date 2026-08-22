package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.port.ShopRequestIndexSyncPort;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.repository.StorePriceVerificationRepository;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 매장 가격 인증 워크플로(요청·승인·반려·취소)의 단일 소유자.
 *
 * <p><b>왜 shop이 아니라 product 컨텍스트가 소유하는가</b>: 승인이 하는 일의 본체는
 * {@code PRODUCT_PRICE}의 매장가·픽업가를 채우는 것이다. 요청 애그리거트를 shop에 두면 그 승인 경로가
 * {@code product.model}·{@code product.repository}를 import해야 해 컨텍스트 경계 규칙
 * ({@code ContextBoundaryTest})을 위반하는데, 그 봉인 목록은 늘릴 수 없다. 그래서 <b>인증 요청
 * 애그리거트 자체를 product가 소유</b>하고({@code PRODUCT_PRICE}와 같은 컨텍스트에 둔다), 가게 단위
 * 상태인 인증 ON/OFF 플래그만 {@link StorePriceVerificationPort}로 다룬다 — 이 방향이 경계 위반 없이
 * 성립하는 유일한 배치다.
 *
 * <p>테이블명이 {@code SHOP_STORE_PRICE_VERIFICATION}인 것은 그 요청이 <b>가게 단위</b>로 접수되기
 * 때문이며, 소유 컨텍스트와는 별개다(엔티티 매핑이 테이블명을 명시한다).
 *
 * <p><b>승인은 요청 시점의 매장가를 쓴다</b> — 항목({@code StorePriceVerificationItem})에 박제된
 * 값이며, 승인 시점에 현재 가격을 다시 읽지 않는다. 그러지 않으면 검수자가 보지 않은 값이 승인된다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가 담당한다.
 */
public class StorePriceVerificationService {

    /** 검수를 막는 상태 — 대기·진행 중이면 재요청할 수 없다. */
    private static final List<StorePriceVerificationStatus> OPEN_STATUSES =
        List.of(StorePriceVerificationStatus.PENDING, StorePriceVerificationStatus.IN_PROGRESS);

    private final StorePriceVerificationRepository verificationRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final StorePriceVerificationPort storePriceVerificationPort;
    private final ShopRequestIndexSyncPort shopRequestIndexSyncPort;

    public StorePriceVerificationService(
        StorePriceVerificationRepository verificationRepository,
        ProductPriceRepository productPriceRepository,
        ProductRepository productRepository,
        StorePriceVerificationPort storePriceVerificationPort,
        ShopRequestIndexSyncPort shopRequestIndexSyncPort
    ) {
        this.verificationRepository = verificationRepository;
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
        this.storePriceVerificationPort = storePriceVerificationPort;
        this.shopRequestIndexSyncPort = shopRequestIndexSyncPort;
    }

    /**
     * 매장 가격 인증을 요청한다.
     *
     * <p>검증 순서 — 검수 중 재요청 차단 → 대상 비어있음 → 할인 진행 → 대상 소유권·가격 행 확인이다.
     * 재요청 차단을 가장 앞에 두어, 이미 검수 중인 점주가 항목을 고쳐 보내도 같은 사유로 일관되게
     * 거절되게 한다.
     *
     * @param items 인증할 (메뉴, 가격 행, 매장가, 픽업가 동일 설정) 목록
     * @return 생성된 인증 요청
     */
    public StorePriceVerification request(
        ShopId shopId,
        UploadedFileId priceListFileId,
        List<StorePriceVerificationItemSpec> items,
        Long requestedByCeoId
    ) {
        if (verificationRepository.existsByShopIdAndStatusIn(shopId, OPEN_STATUSES)) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_IN_PROGRESS);
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }

        // 대상 메뉴와 가격 행을 먼저 전부 확인한다 — 요청 행을 만든 뒤 중간에서 실패하면 고아 요청이 남는다.
        List<ResolvedItem> resolved = resolveItems(shopId, items);

        StorePriceVerification saved = verificationRepository.save(
            StorePriceVerification.of(shopId, priceListFileId, requestedByCeoId));

        for (ResolvedItem item : resolved) {
            verificationRepository.saveItem(StorePriceVerificationItem.of(
                saved.getVerificationId(),
                item.productId(),
                item.productPriceId(),
                item.storePrice(),
                item.applyPickupSamePrice()
            ));
        }
        return saved;
    }

    /**
     * 대상 항목을 검증해 확정한다 — 메뉴 소유권·가격 행 소속·할인 진행 여부를 함께 본다.
     *
     * <p>가격 행이 정말 그 메뉴의 것인지 확인하는 것이 핵심이다. 확인하지 않으면 남의 메뉴 가격 행에
     * 매장가를 심을 수 있다({@code OrderProductValidationService}가 옵션의 그룹 소속을 확인하는 것과
     * 같은 종류의 방어다).
     */
    private List<ResolvedItem> resolveItems(ShopId shopId, List<StorePriceVerificationItemSpec> items) {
        List<ResolvedItem> resolved = new ArrayList<>();
        Map<Long, Product> productCache = new LinkedHashMap<>();

        for (StorePriceVerificationItemSpec item : items) {
            ProductId productId = ProductId.of(item.productId());
            Product product = productCache.computeIfAbsent(item.productId(),
                key -> loadOwnedProduct(shopId, productId));

            // 할인 중인 메뉴는 인증 요청 대상이 아니다 — 승인 시 매장가를 반영하면 할인가와 뒤엉킨다.
            // 판정식은 ProductPriceService와 같다(이 저장소에 할인 스케줄링이 없어 "할인가 존재"로 본다).
            if (product.getDiscountPrice() != null) {
                throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_DISCOUNT_IN_PROGRESS,
                    ErrorCode.SHOP_STORE_PRICE_VERIFICATION_DISCOUNT_IN_PROGRESS.getDefaultMessage()
                        + ": " + product.getName());
            }

            ProductPrice price = productPriceRepository.findById(ProductPriceId.of(item.priceId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));
            // ★ 가격 행이 그 메뉴의 것인지 확인한다. 없으면 남의 메뉴 가격에 매장가를 심는 것이다.
            if (!price.getProductId().equals(productId)) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND);
            }

            resolved.add(new ResolvedItem(
                productId,
                price.getProductPriceId(),
                item.storePrice(),
                item.applyPickupSamePrice()
            ));
        }
        return resolved;
    }

    /** 관리자가 검수에 착수한다({@code PENDING} → {@code IN_PROGRESS}). */
    public void startReview(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.startReview(now);
        verificationRepository.save(verification);
        syncIndex(verification, null);
    }

    /**
     * 승인한다 — <b>요청에 담긴 매장가를 각 가격 행에 반영하고 가게 인증을 켠다.</b>
     *
     * <p>{@code applyPickupSamePrice}가 켜진 항목은 픽업가도 매장가와 같게 설정된다. 승인 즉시 메뉴
     * 가격에 반영되는 것이 요구사항이므로 배치·비동기로 미루지 않는다.
     *
     * <p><b>인증을 켜기 전에 항목을 먼저 반영한다.</b> 순서를 뒤집으면 반영 도중 실패했을 때 인증만
     * 켜진 채 매장가가 비어 있는 상태가 남는다(같은 트랜잭션이라 롤백되지만, 순서가 의도를 드러낸다).
     */
    public void approve(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);

        for (StorePriceVerificationItem item : verificationRepository
            .findAllItemsByVerificationId(verificationId)) {
            ProductPrice price = productPriceRepository.findById(item.getProductPriceId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));
            price.applyVerifiedStorePrice(item.getStorePrice(), item.isApplyPickupSamePrice(), now);
            productPriceRepository.save(price);
        }

        verification.approve(now);
        verificationRepository.save(verification);
        syncIndex(verification, null);

        storePriceVerificationPort.verifyStorePrice(verification.getShopId().value());
    }

    /** 반려한다. 사유는 필수다 — 무엇이 부적합했는지 알아야 다시 요청할 수 있다. */
    public void reject(StorePriceVerificationId verificationId, String rejectReason, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.reject(rejectReason, now);
        verificationRepository.save(verification);
        syncIndex(verification, rejectReason);
    }

    /** 점주가 검수 대기·진행 중인 요청을 취소한다. */
    public void cancel(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.cancel(now);
        verificationRepository.save(verification);
        syncIndex(verification, null);
    }

    /**
     * 가게의 미인증 메뉴와 그 사유를 모아 돌려준다(점주 화면의 "인증 OFF 사유 표시").
     *
     * <p>사유가 없는(정상) 가격 행은 담기지 않는다. 한 메뉴에 가격 행이 여러 개면 <b>가장 먼저 발견된
     * 사유 하나만</b> 담는다 — 점주에게 같은 메뉴가 여러 줄로 반복 표시되면 무엇을 고쳐야 하는지가
     * 오히려 흐려진다.
     */
    public List<StorePriceUnverifiedItem> findUnverifiedItems(ShopId shopId) {
        Map<Long, StorePriceUnverifiedItem> byProductId = new LinkedHashMap<>();

        for (ProductPrice price : productPriceRepository.findAllByShopId(shopId)) {
            var reason = price.resolveUnverifiedReason();
            if (reason == null) {
                continue;
            }
            Long productId = price.getProductId().value();
            if (byProductId.containsKey(productId)) {
                continue;
            }
            Product product = productRepository.findById(price.getProductId()).orElse(null);
            if (product == null || product.isDeleted()) {
                continue;
            }
            byProductId.put(productId, new StorePriceUnverifiedItem(productId, product.getName(), reason));
        }
        return List.copyOf(byProductId.values());
    }

    /**
     * 상태 전이를 통합 요청처리 인덱스에 반영한다 — 원본 전이와 <b>같은 트랜잭션</b>에서 동기 기록한다.
     *
     * <p>전이 메서드마다 이 호출을 넣는 이유는, 한 곳이라도 빠지면 점주 화면의 요청처리 현황이 원본과
     * 영구히 어긋나기 때문이다(인덱스 행을 찾지 못하면 {@code SHOP_REQUEST_NOT_FOUND}로 원본까지
     * 롤백되므로 누락이 조용히 묻히지 않는다).
     *
     * <p><b>배선이 api 모듈이 아니라 이 도메인 서비스에 있는 것이 중요하다</b> — 승인·반려는 admin이,
     * 취소는 ceo가 호출하므로 api 모듈에 두면 같은 전이가 두 모듈로 흩어져 한쪽이 반드시 빠진다
     * ({@code ShopRequestIndexRecorder} javadoc의 배선 원칙).
     *
     * <p>기록자를 직접 부르지 않고 {@link ShopRequestIndexSyncPort}를 경유하는 것은 컨텍스트 경계
     * 때문이다 — 그 기록자는 shop 컨텍스트 소유다.
     */
    private void syncIndex(StorePriceVerification verification, String rejectReason) {
        shopRequestIndexSyncPort.syncStorePriceVerificationStatus(
            verification.getId(),
            toShopRequestStatusName(verification.getStatus()),
            rejectReason
        );
    }

    /**
     * 인증 상태 → 통합 상태 매핑. <b>이 매핑을 product가 소유하는 이유는 컨텍스트 경계다</b> —
     * shop이 {@code StorePriceVerificationStatus}(product 소유)를 알면
     * {@code ContextBoundaryTest}를 위반하므로, 변환은 enum을 소유한 이쪽에서 수행해 통합 상태만
     * 넘긴다({@code ReviewBlindRequestService}가 확립한 방식과 같다).
     *
     * <p>다섯 상태가 이름까지 그대로 대응해 접는 값이 없다 — 인증 검수가 통합 인덱스와 같은
     * 생애주기(대기 → 진행 → 승인·반려·취소)를 갖기 때문이다. 그래도 {@code name()}을 그대로
     * 흘려보내지 않고 명시 매핑을 두는 이유는, 어느 한쪽 enum에 상수가 추가되면 <b>컴파일 단계에서</b>
     * 대응을 결정하도록 강제하기 위해서다.
     */
    private static String toShopRequestStatusName(StorePriceVerificationStatus status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case IN_PROGRESS -> "IN_PROGRESS";
            case APPROVED -> "APPROVED";
            case REJECTED -> "REJECTED";
            case CANCELED -> "CANCELED";
        };
    }

    private StorePriceVerification loadVerification(StorePriceVerificationId verificationId) {
        return verificationRepository.findById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));
    }

    /**
     * 대상 메뉴가 정말 그 가게 것인지 확인하며 로드한다. "메뉴 없음"과 "남의 가게 메뉴"를 같은
     * {@code PRODUCT_NOT_FOUND}(404)로 합쳐 존재 여부가 새어 나가지 않게 한다.
     */
    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    /**
     * 검증을 통과한 인증 대상 항목 — 요청 행을 만들기 전에 전부 확정해 두기 위한 내부 표현이다.
     */
    private record ResolvedItem(
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
    }
}
