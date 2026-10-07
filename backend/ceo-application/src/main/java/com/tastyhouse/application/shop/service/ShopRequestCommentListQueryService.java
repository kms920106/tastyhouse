package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;

@Service
@Transactional(readOnly = true)
class ShopRequestCommentListQueryService implements ShopRequestCommentListQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestCommentListQueryService(
        ShopRequestQueryPort shopRequestQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestQueryPort = shopRequestQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopRequestCommentResult> getComments(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return withAuthorTypeDescriptions(shopRequestQueryPort.findComments(detail.requestId()));
    }

    private static List<ShopRequestCommentResult> withAuthorTypeDescriptions(List<ShopRequestCommentResult> comments) {
        return comments.stream()
            .map(comment -> comment.withAuthorTypeDescription(comment.authorType() == null
                ? null
                : ShopRequestCommentAuthorType.valueOf(comment.authorType()).getDescription()))
            .toList();
    }
}
