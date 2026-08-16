package com.tastyhouse.webapi.menureview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 메뉴 평가 명령 서비스(web).
 *
 * <p>주문 항목당 1건 불변식·소유권 검증·재집계 이벤트 발행은 도메인 서비스
 * {@link MenuReviewLifecycleService}가 갖고, 이 서비스는 트랜잭션 경계 선언과 HTTP 경계 타입 승격,
 * 그리고 <b>타 컨텍스트(order·product) 데이터를 요구하는 등록 전 검증</b>을 담당한다.
 *
 * <p>등록 검증을 여기 두는 이유: 주문 항목의 존재·주문 소유자·상품의 평가 제외 여부는 order·product
 * 컨텍스트의 데이터라, 도메인 서비스에서 판정하면 menureview 컨텍스트가 그 두 컨텍스트의 모델·리포지토리를
 * 직접 참조하게 되어 경계 규칙({@code ContextBoundaryTest}) 위반이 된다.
 *
 * <p><b>{@code shopId}·{@code productId}·{@code orderId}는 요청에서 받지 않고 서버가
 * {@code ORDER_PRODUCT}·{@code ORDERS}에서 읽어 채운다</b> — 클라이언트가 보낸 값을 신뢰하면 다른 상품에
 * 평점을 붙일 수 있다.
 *
 * <p><b>매장 리뷰 존재 여부는 확인하지 않는다</b> — 매장 리뷰가 없어도 메뉴 평가는 등록된다.
 *
 * <p>CQRS 교차 주입 금지에 따라 infra query DAO도 같은 모듈의 {@code *QueryService}도 주입하지 않으며,
 * 모든 명령은 식별자만 반환한다.
 */
@Service
@Transactional
public class MenuReviewCommandService {

    private final MenuReviewLifecycleService menuReviewLifecycleService;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public MenuReviewCommandService(
        MenuReviewLifecycleService menuReviewLifecycleService,
        OrderProductRepository orderProductRepository,
        OrderRepository orderRepository,
        ProductRepository productRepository
    ) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
        this.orderProductRepository = orderProductRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * 메뉴 평가 등록.
     *
     * <p>검증 순서는 스펙 그대로다 — (1) 주문 항목 존재({@code ORDER_PRODUCT_NOT_FOUND} 404),
     * (2) 그 주문이 요청 회원의 것인지({@code MENU_REVIEW_ACCESS_DENIED} 403, IDOR 방어),
     * (3) 상품이 평가 제외 대상이 아닌지({@code MENU_REVIEW_NOT_ALLOWED} 400). 중복 판정(409)은 도메인
     * 서비스가 수행한다.
     *
     * @return 생성된 메뉴 평가 식별자
     */
    public Long createMenuReview(Long memberId, Long orderProductId, Integer rating, String comment) {
        OrderProduct orderProduct = orderProductRepository.findById(OrderProductId.of(orderProductId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Order order = orderRepository.findById(orderProduct.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        MemberId targetMemberId = MemberId.of(memberId);
        if (!order.getMemberId().equals(targetMemberId)) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
        }

        Product product = productRepository.findById(orderProduct.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isRatingExcluded()) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_NOT_ALLOWED);
        }

        return menuReviewLifecycleService.register(
            targetMemberId,
            order.getShopId(),
            orderProduct.getProductId(),
            orderProduct.getOrderId(),
            orderProduct.getOrderProductId(),
            rating,
            comment
        );
    }

    /**
     * 메뉴 평가 수정 — 본인 평가만 수정할 수 있다(소유권 검증은 도메인 서비스가 수행).
     */
    public void updateMenuReview(Long id, Long memberId, Integer rating, String comment) {
        MenuReviewId menuReviewId = MenuReviewId.of(id);
        menuReviewLifecycleService.modify(menuReviewId, MemberId.of(memberId), rating, comment);
    }

    /**
     * 메뉴 평가 삭제 — 본인 평가만 삭제할 수 있다. 삭제되면 그 주문 항목에 다시 평가를 남길 수 있다.
     */
    public void deleteMenuReview(Long id, Long memberId) {
        MenuReviewId menuReviewId = MenuReviewId.of(id);
        menuReviewLifecycleService.remove(menuReviewId, MemberId.of(memberId));
    }
}
