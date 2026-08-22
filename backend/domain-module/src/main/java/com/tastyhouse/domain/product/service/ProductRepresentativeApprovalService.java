package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 추천(대표 메뉴) 승인 워크플로의 단일 소유자.
 *
 * <p><b>지정은 승인을 거치고 해제는 즉시 반영된다.</b> 비대칭인 이유는 검수의 목적이 "부적합한 메뉴가
 * 가게 상단에 노출되는 것"을 막는 데 있기 때문이다 — 해제는 그 위험이 없는 방향이라 점주가 즉시
 * 내릴 수 있어야 한다({@code ProductVegetarianApprovalService}의 채식 해제와 같은 판단).
 *
 * <p>진실원은 {@code Product.representative} 하나다. 대표 메뉴 목록 테이블을 따로 만들지 않고
 * 승인 시 그 컬럼을 켜므로, "현재 이 가게의 대표 메뉴는 무엇인가"는 언제나 {@code PRODUCT} 한 곳만
 * 보면 된다.
 */
public class ProductRepresentativeApprovalService {

    /**
     * 가게당 대표 메뉴 상한. PDF 등록 기준 — 선택지가 많으면 오히려 고민이 늘어난다.
     */
    private static final long MAX_REPRESENTATIVE_COUNT = 6L;

    private final ProductRepresentativeRequestRepository requestRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductRepresentativeApprovalService(
        ProductRepresentativeRequestRepository requestRepository,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository
    ) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * 대표 메뉴 지정을 신청한다. 여러 메뉴를 한 번에 신청하므로 개수 제한 판정도 <b>요청 전체를 반영한
     * 뒤의 최종 상태</b> 기준이다 — 하나씩 순차로 검사하면 배열 순서에 따라 어느 건이 통과할지가 갈린다
     * ({@code ProductAvailabilityService#hideProducts}와 같은 판정 방식).
     *
     * <p>제한 계산에 <b>대기 중인 요청 건수를 포함</b>하는 것이 이 메서드의 핵심이다. 이미 켜진 대표
     * 메뉴만 세면 점주가 PENDING을 여러 벌 쌓아 두고 관리자가 순차 승인하는 사이에 6개를 넘길 수 있다
     * — 승인 시점에는 각 건이 개별적으로 통과하므로 그때는 막을 방법이 없다.
     *
     * @param shopId     소유 가게. 호출부(ceo-api)가 소유권을 검증한 뒤 넘긴다
     * @param productIds 지정할 메뉴들. 중복은 제거되고, 이미 대표인 메뉴·대기 중인 메뉴는 건너뛴다(멱등)
     * @return 생성된 요청 식별자들. 건너뛴 메뉴 몫은 담기지 않는다
     */
    public List<Long> requestRepresentative(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        if (distinctIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        // 실제로 새 요청을 만들 대상만 남긴다 — 이미 대표거나 대기 중인 건은 개수에도 다시 세지 않는다
        // (그러면 같은 메뉴가 "현재 대표"와 "이번 신청" 양쪽으로 두 번 계산돼 제한이 실제보다 빡빡해진다).
        List<Product> targets = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            Product product = loadOwnedProduct(shopId, productId);
            if (product.isRepresentative()) {
                continue;
            }
            if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
                continue;
            }
            requireHasImage(productId);
            targets.add(product);
        }

        validateLimit(shopId, targets.size());

        List<Long> requestIds = new ArrayList<>();
        for (Product target : targets) {
            ProductRepresentativeRequest saved = requestRepository.save(
                ProductRepresentativeRequest.of(target.getProductId(), shopId));
            requestIds.add(saved.getId());
        }
        return requestIds;
    }

    /**
     * 승인한다 — {@code Product.representative}를 켠다.
     *
     * <p>승인 시점에 개수 제한을 <b>다시 검증한다.</b> 신청 시점의 검증만으로는 부족하다 — 여러 건이
     * 대기 중인 사이 점주가 해제 없이 다른 경로로 대표 메뉴를 늘렸거나, 신청 이후 시간이 흘러 상태가
     * 달라졌을 수 있다. 승인이 컬럼을 켜는 유일한 지점이므로 최종 방어선도 여기다.
     */
    public void approve(ProductRepresentativeRequestId requestId) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        Product product = loadProduct(request.getProductId());

        if (!product.isRepresentative()) {
            // 이미 대표인 메뉴의 요청이면 개수가 늘지 않으므로 검증 대상이 아니다(멱등).
            //
            // 이 시점의 요청은 아직 PENDING이라 대기 건수에 자기 자신이 포함된다. 그래서 신청 시점과
            // 같은 식(현재 + 대기 + 1)을 쓰면 자기 자신을 두 번 세어, 6개를 정상 접수한 점주의 요청이
            // 첫 승인부터 전부 거부된다. 승인은 "지금 켜는 1개"만 더하면 되므로 대기 건수를 빼고 센다.
            validateApprovableLimit(request.getShopId());
        }
        requireHasImage(request.getProductId());

        request.approve();
        requestRepository.save(request);

        product.changeRepresentative(true);
        productRepository.save(product);
    }

    /** 반려한다. 사유는 필수다 — 왜 부적합했는지 알아야 다시 신청할 수 있다. */
    public void reject(ProductRepresentativeRequestId requestId, String rejectReason) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    /** 점주가 검수 대기 중인 신청을 취소한다. */
    public void cancel(ProductRepresentativeRequestId requestId) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    /**
     * 대표 메뉴 지정을 해제한다. <b>승인을 거치지 않고 즉시 반영된다.</b>
     *
     * <p>단 <b>가게마다 최소 1개는 남아야 한다.</b> 이 불변식은 새로 만드는 것이 아니라 일괄 숨김이
     * 이미 쓰고 있는 {@link ErrorCode#PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE}(400)를 재사용한다 —
     * 같은 불변식에 코드를 둘로 두면 "숨김으로는 막히는 상태를 해제로는 만들 수 있는" 불일치가 생기고,
     * 프론트가 두 코드를 각각 분기해야 한다.
     *
     * <p>이미 해제 상태여도 실패가 아니다(멱등) — 그때는 개수가 줄지 않으므로 검증도 건너뛴다.
     */
    public void clearRepresentative(ShopId shopId, ProductId productId) {
        Product product = loadOwnedProduct(shopId, productId);
        if (!product.isRepresentative()) {
            return;
        }

        // 노출 중인 대표 메뉴 기준으로 센다 — 일괄 숨김의 판정({@code countVisibleRepresentativeByShopId})과
        // 같은 집합을 봐야 두 경로가 같은 하한을 공유한다.
        if (productRepository.countVisibleRepresentativeByShopId(shopId) <= 1L) {
            throw new BusinessException(ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        }

        product.changeRepresentative(false);
        productRepository.save(product);
    }

    /**
     * 이 가게가 대표 메뉴를 {@code additional}개 더 가질 수 있는지 판정한다.
     *
     * <p>계산식은 {@code 현재 대표 메뉴 수 + 대기 중인 요청 수 + 이번 요청 수 <= 6}이다.
     *
     * <p>현재 수는 <b>노출 여부와 무관하게</b> 센다({@code countRepresentativeByShopId}) — 숨김을 빼고
     * 세면 6개를 채운 점주가 3개를 숨기고 3개를 더 승인받은 뒤, 숨김을 해제해 9개를 노출시킬 수 있다.
     * 해제 경로({@link #clearRepresentative})가 노출분만 세는 것과 의도적으로 다르다. 상한은 전체
     * 집합에 걸리고, 하한("최소 1개")은 손님에게 보이는 집합에 걸리기 때문이다.
     */
    private void validateLimit(ShopId shopId, int additional) {
        if (additional <= 0) {
            return;
        }
        long current = productRepository.countRepresentativeByShopId(shopId);
        long pending = requestRepository.countByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
        if (current + pending + additional > MAX_REPRESENTATIVE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 승인으로 대표 메뉴를 1개 더 켤 수 있는지 판정한다.
     *
     * <p>{@link #validateLimit}과 달리 <b>대기 건수를 세지 않는다</b> — 승인 대상 요청 자신이 아직
     * PENDING이므로 함께 세면 이중 계상이 된다. 승인 시점에 늘어나는 것은 지금 켜는 1개뿐이다.
     */
    private void validateApprovableLimit(ShopId shopId) {
        if (productRepository.countRepresentativeByShopId(shopId) + 1 > MAX_REPRESENTATIVE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 이미지가 등록된 메뉴만 대표로 지정할 수 있다 — 이미지 없는 대표 메뉴는 주문을 망설이게 한다(PDF 기준).
     *
     * <p>판정에 대표 이미지(노출 중 최소 sort) 조회를 쓴다. 이미지가 있어도 전부 숨김이면 손님 화면에는
     * 아무것도 보이지 않으므로, "행이 있는가"가 아니라 "노출되는 이미지가 있는가"가 옳은 기준이다.
     */
    private void requireHasImage(ProductId productId) {
        if (productImageRepository.findRepresentativeImageFileId(productId) == null) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_IMAGE_REQUIRED);
        }
    }

    private ProductRepresentativeRequest loadRequest(ProductRepresentativeRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 대상 메뉴가 정말 그 가게 것인지 확인하며 로드한다.
     *
     * <p>"메뉴 없음"과 "남의 가게 메뉴"를 같은 {@code PRODUCT_NOT_FOUND}(404)로 합친다 — 코드가
     * 갈리면 남의 가게 메뉴의 존재 여부가 새어 나가 식별자 열거에 쓰인다.
     */
    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private List<ProductId> distinct(List<ProductId> productIds) {
        return productIds == null
            ? List.of()
            : productIds.stream().filter(Objects::nonNull).distinct().toList();
    }
}
