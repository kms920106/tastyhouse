package com.tastyhouse.domain.product.service;

import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 채식 설정 승인 워크플로의 단일 소유자.
 *
 * <p><b>채식 설정은 점주가 직접 켤 수 없다.</b> 점주는 재료를 근거로 신청만 하고, 관리자가 그 재료를
 * 보고 판정해야 반영된다({@code Product#applyVegetarianType}) — 채식 표기는 알레르기·신념과 직결돼
 * 잘못된 표기의 대가가 크기 때문이다.
 *
 * <p>승인 결과의 진실원은 {@code Product.vegetarianType} 하나다. 요청 이력이 여러 건 쌓여도
 * "현재 이 메뉴가 채식인가"는 그 한 곳만 보면 된다.
 */
public class ProductVegetarianApprovalService {

    /**
     * 채식 메뉴를 등록할 수 없는 가게 카테고리 이름.
     *
     * <p>가게 카테고리가 자유 입력 문자열이라 기계 판별이 불가능해 <b>이름 목록</b>으로 둔다.
     * 이 목록은 요청 시점에 <b>거절 근거</b>로만 쓰이므로, 목록에 없는 새 카테고리는 통과한 뒤
     * 관리자 검수에서 걸러진다 — 즉 이 목록이 최후 방어선이 아니라 명백한 경우의 조기 차단이다.
     */
    private static final Set<String> DISALLOWED_SHOP_CATEGORIES = Set.of(
        "돈까스/회/일식", "고기/구이", "찜/탕/찌개", "족발/보쌈", "피자", "치킨", "중식", "야식"
    );

    private final ProductVegetarianRequestRepository requestRepository;
    private final ProductRepository productRepository;

    public ProductVegetarianApprovalService(
        ProductVegetarianRequestRepository requestRepository,
        ProductRepository productRepository
    ) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
    }

    /**
     * 채식 설정을 신청한다.
     *
     * @param shopCategoryNames 이 가게의 카테고리 이름들. 호출부(ceo-api)가 shop 컨텍스트에서 읽어
     *                          넘긴다 — product가 shop 모델을 직접 참조하면 컨텍스트 경계 위반이다.
     * @param ingredients       채소 외 포함 재료. <b>검수의 유일한 근거</b>라 필수다.
     * @return 생성된 요청 식별자
     */
    public Long requestVegetarian(
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        Set<String> shopCategoryNames
    ) {
        loadProduct(productId);
        validateShopCategoryAllowed(shopCategoryNames);

        if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_ALREADY_PENDING);
        }

        ProductVegetarianRequest saved = requestRepository.save(
            ProductVegetarianRequest.of(productId, vegetarianType, ingredients, description));
        return saved.getId();
    }

    /** 승인한다 — 요청된 채식 단계를 메뉴에 반영한다. */
    public void approve(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.approve();
        requestRepository.save(request);

        Product product = loadProduct(request.getProductId());
        product.applyVegetarianType(request.getVegetarianType());
        productRepository.save(product);
    }

    /** 반려한다. 사유는 필수다 — 어느 재료가 문제였는지 알아야 다시 신청할 수 있다. */
    public void reject(ProductVegetarianRequestId requestId, String rejectReason) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    /** 점주가 검수 대기 중인 신청을 취소한다. */
    public void cancel(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    /**
     * 채식 설정을 해제한다.
     *
     * <p><b>해제는 승인을 거치지 않는다.</b> 승인이 필요한 이유는 "채식이 아닌 메뉴가 채식으로
     * 표기되는 것"을 막기 위함인데, 해제는 그 위험이 없는 방향이다 — 오히려 잘못된 표기를 점주가
     * 즉시 내릴 수 있어야 한다.
     */
    public void clearVegetarian(ProductId productId) {
        Product product = loadProduct(productId);
        product.applyVegetarianType(null);
        productRepository.save(product);
    }

    /**
     * 이 가게 카테고리로 채식 설정을 신청할 수 있는지 조회한다 — {@link #validateShopCategoryAllowed}와
     * 같은 판정({@link #DISALLOWED_SHOP_CATEGORIES})을 예외 없이 boolean으로 재사용한다. 조회
     * 경로(현황 화면의 "신청 가능 여부" 표시)에서 쓴다.
     */
    public boolean isShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (shopCategoryNames == null || shopCategoryNames.isEmpty()) {
            return true;
        }
        return shopCategoryNames.stream().noneMatch(DISALLOWED_SHOP_CATEGORIES::contains);
    }

    private void validateShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (!isShopCategoryAllowed(shopCategoryNames)) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_CATEGORY_NOT_ALLOWED);
        }
    }

    private ProductVegetarianRequest loadRequest(ProductVegetarianRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
