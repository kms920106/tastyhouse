package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopRequestCommentQueryService implements ShopRequestCommentQueryUseCase {

    private final ShopRequestManagementQueryPort shopRequestManagementQueryPort;

    public ShopRequestCommentQueryService(ShopRequestManagementQueryPort shopRequestManagementQueryPort) {
        this.shopRequestManagementQueryPort = shopRequestManagementQueryPort;
    }

    @Override
    public List<ShopRequestCommentResult> getComments(Long requestId) {
        shopRequestManagementQueryPort.findRequestDetail(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return withAuthorTypeDescriptions(shopRequestManagementQueryPort.findComments(requestId));
    }

    private static List<ShopRequestCommentResult> withAuthorTypeDescriptions(List<ShopRequestCommentResult> comments) {
        return comments.stream()
            .map(comment -> comment.withAuthorTypeDescription(comment.authorType() == null
                ? null
                : ShopRequestCommentAuthorType.valueOf(comment.authorType()).getDescription()))
            .toList();
    }
}
