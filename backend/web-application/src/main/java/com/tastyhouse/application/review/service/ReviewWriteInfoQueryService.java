package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.order.port.out.OrderProductOwnershipResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.review.port.in.ReviewWriteInfoQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class ReviewWriteInfoQueryService implements ReviewWriteInfoQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;
    private final ProductQueryPort productQueryPort;
    private final OrderQueryPort orderQueryPort;

    public ReviewWriteInfoQueryService(
        ReviewQueryPort reviewQueryPort,
        ProductQueryPort productQueryPort,
        OrderQueryPort orderQueryPort
    ) {
        this.reviewQueryPort = reviewQueryPort;
        this.productQueryPort = productQueryPort;
        this.orderQueryPort = orderQueryPort;
    }

    @Override
    public ReviewWriteInfoView getReviewWriteInfo(Long orderProductId, Long memberId) {
        if (memberId == null) {
            throw new ApplicationException(WebErrorCode.AUTH_REQUIRED);
        }

        OrderProductOwnershipResult ownership = orderQueryPort.findOrderProductOwnership(orderProductId)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));

        if (ownership.orderMemberId() == null) {
            throw new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND);
        }
        if (!ownership.orderMemberId().equals(memberId)) {
            throw new ApplicationException(WebErrorCode.REVIEW_ORDER_ACCESS_DENIED);
        }

        ProductDetailResult product = productQueryPort.findProductDetailById(ownership.productId())
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.discountPrice() != null
            ? product.discountPrice()
            : product.originalPrice();

        boolean reviewed = reviewQueryPort.existsByOrderIdAndProductIdAndMemberId(
            ownership.orderId(), ownership.productId(), memberId
        );

        return new ReviewWriteInfoView(
            product.id(),
            product.name(),
            getFirstImageUrl(product.id()),
            price,
            ownership.orderId(),
            reviewed,
            ownership.orderMethod()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryPort.findProductImageUrls(productId).stream()
            .findFirst()
            .orElse(null);
    }
}
