package com.tastyhouse.domain.order.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.coupon.service.CouponUseResult;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 접수 불변식(도메인 서비스).
 *
 * <p>주문 한 건의 접수는 {@code Order} 헤더 · 상품 라인({@code OrderProduct}) · 라인 옵션
 * ({@code OrderProductOption}) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을
 * 헤더에 되반영해야 하는 원자 연산이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고,
 * 금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다. 여기에 판매중지 검증 · 쿠폰 사용
 * ({@link CouponIssueService}) · 포인트 차감({@link PointLedgerService})까지 같은 트랜잭션에 묶이는
 * 크로스 애그리거트 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code OrderCommandService})가 선언한다.
 *
 * <p>주문 접수는 도메인 이벤트를 발행하지 않는다 — 과거 {@code OrderCreatedEvent}를 발행했으나 수신
 * 리스너가 없는 no-op이어서 P9(도메인 이벤트 정비)에서 제거했다. 접수 이후 비동기 후처리(알림·집계)가
 * 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다. 상태 전이({@code confirm}/{@code cancel})
 * 이벤트도 소비 수요가 생길 때 {@code OrderTransitionService}의 전이 지점에 추가한다(현재는 YAGNI).
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다
 * (헤더는 신규 저장 후 금액 갱신으로 2회, 상품 라인은 신규 저장 후 가격 갱신으로 2회 저장한다).
 *
 * <p>반환은 생성된 주문의 식별자({@link OrderId})만이다 — 응답 조립(가게명·상품 라인·결제 요약)은
 * 커밋 이후 소비 모듈의 {@code OrderQueryService}가 infra query DAO로 재조회해 담당한다(CQRS 분리).
 */
public class OrderPlacementService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CouponIssueService couponIssueService;
    private final PointLedgerService pointLedgerService;

    public OrderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService
    ) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderProductOptionRepository = orderProductOptionRepository;
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.couponIssueService = couponIssueService;
        this.pointLedgerService = pointLedgerService;
    }

    /**
     * 주문을 접수한다 — 가게·회원 존재 확인 → 주문 헤더 생성 → 상품 라인·옵션 생성과 금액 집계 →
     * 쿠폰·포인트 사용 → 금액 대조 검증 → 헤더 금액 반영.
     *
     * @return 생성된 주문 식별자
     */
    public OrderId place(MemberId memberId, OrderPlacement placement) {
        ShopId shopId = ShopId.of(placement.shopId());
        if (shopRepository.findById(shopId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND);
        }
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        Order order = Order.of(
            memberId,
            shopId,
            generateOrderNumber(),
            placement.orderMethod(),
            OrderStatus.PENDING,
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername(),
            0, 0, 0, 0, 0, 0, null, 0, 0
        );
        Order savedOrder = orderRepository.save(order);

        int totalProductAmount = 0;
        int productDiscountAmount = 0;

        for (OrderPlacementItem item : placement.items()) {
            Product product = productRepository.findById(ProductId.of(item.productId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                    ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + item.productId()));

            if (product.isSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
            }

            String productImageFilePath = productImageRepository.findRepresentativeImageFilePath(product.getProductId());
            int originalPrice = product.getOriginalPrice();
            Integer discountPrice = product.getDiscountPrice();

            OrderProduct orderProduct = OrderProduct.of(
                savedOrder.getOrderId(),
                product.getProductId(),
                product.getName(),
                productImageFilePath,
                item.quantity(),
                originalPrice,
                discountPrice,
                0, 0
            );
            OrderProduct savedOrderProduct = orderProductRepository.save(orderProduct);

            int totalOptionPrice = saveSelectedOptions(savedOrderProduct, item);

            int effectivePrice = discountPrice != null ? discountPrice : originalPrice;
            int itemTotal = (effectivePrice + totalOptionPrice) * item.quantity();
            int itemDiscount = discountPrice != null ? (originalPrice - discountPrice) * item.quantity() : 0;

            savedOrderProduct.updatePrices(totalOptionPrice, itemTotal);
            orderProductRepository.save(savedOrderProduct);

            totalProductAmount += originalPrice * item.quantity() + totalOptionPrice * item.quantity();
            productDiscountAmount += itemDiscount;
        }

        int couponDiscountAmount = 0;
        MemberCouponId memberCouponId = null;
        if (placement.memberCouponId() != null) {
            int orderAmountAfterProductDiscount = totalProductAmount - productDiscountAmount;
            CouponUseResult couponResult = couponIssueService.useCoupon(
                MemberCouponId.of(placement.memberCouponId()), memberId, orderAmountAfterProductDiscount
            );
            couponDiscountAmount = couponResult.couponDiscountAmount();
            memberCouponId = MemberCouponId.of(couponResult.memberCouponId());
        }

        int pointDiscountAmount = 0;
        if (placement.usePoint() > 0) {
            pointDiscountAmount = placement.usePoint();
            pointLedgerService.usePoints(memberId, pointDiscountAmount);
        }

        int totalDiscountAmount = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        int finalAmount = totalProductAmount - totalDiscountAmount;

        validateAmounts(placement, totalProductAmount, totalDiscountAmount, productDiscountAmount,
            couponDiscountAmount, pointDiscountAmount, finalAmount);

        savedOrder.updateAmounts(totalProductAmount, productDiscountAmount, couponDiscountAmount,
            pointDiscountAmount, totalDiscountAmount, finalAmount, memberCouponId, pointDiscountAmount);
        orderRepository.save(savedOrder);

        return savedOrder.getOrderId();
    }

    /**
     * 상품 라인에서 선택된 옵션들을 저장하고 옵션 추가 금액 합계를 돌려준다.
     */
    private int saveSelectedOptions(OrderProduct savedOrderProduct, OrderPlacementItem item) {
        if (item.selectedOptions() == null) {
            return 0;
        }

        int totalOptionPrice = 0;
        for (OrderPlacementItemOption selectedOption : item.selectedOptions()) {
            ProductOptionGroup optionGroup = productOptionGroupRepository
                .findById(ProductOptionGroupId.of(selectedOption.groupId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            ProductOption option = productOptionRepository
                .findById(ProductOptionId.of(selectedOption.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

            orderProductOptionRepository.save(OrderProductOption.of(
                savedOrderProduct.getOrderProductId(),
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice()
            ));

            totalOptionPrice += option.getAdditionalPrice();
        }
        return totalOptionPrice;
    }

    /**
     * 클라이언트가 계산해 보내온 금액과 서버 계산 금액을 항목별로 대조한다 — 불일치는 위조 시도로 보고 거절한다.
     */
    private void validateAmounts(
        OrderPlacement placement,
        int totalProductAmount,
        int totalDiscountAmount,
        int productDiscountAmount,
        int couponDiscountAmount,
        int pointDiscountAmount,
        int finalAmount
    ) {
        if (!placement.totalProductAmount().equals(totalProductAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.totalProductAmount() + ", 계산: " + totalProductAmount);
        }
        if (!placement.productDiscountAmount().equals(productDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.productDiscountAmount() + ", 계산: " + productDiscountAmount);
        }
        if (!placement.couponDiscountAmount().equals(couponDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.couponDiscountAmount() + ", 계산: " + couponDiscountAmount);
        }
        if (!placement.usePoint().equals(pointDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.usePoint() + ", 계산: " + pointDiscountAmount);
        }
        if (!placement.totalDiscountAmount().equals(totalDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.totalDiscountAmount() + ", 계산: " + totalDiscountAmount);
        }
        if (!placement.finalAmount().equals(finalAmount)) {
            throw new BusinessException(ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH,
                ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.finalAmount() + ", 계산: " + finalAmount);
        }
    }

    private String generateOrderNumber() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + dateTime + "-" + uuid;
    }
}
