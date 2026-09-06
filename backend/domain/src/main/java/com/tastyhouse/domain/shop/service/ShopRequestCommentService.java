package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthor;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;

public class ShopRequestCommentService {
    private final ShopRequestCommentRepository shopRequestCommentRepository;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCommentService(
        ShopRequestCommentRepository shopRequestCommentRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopRequestCommentRepository = shopRequestCommentRepository;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    public Long addCommentByCeo(Long requestId, Long shopId, Long ceoId, String content) {
        shopRequestIndexRecorder.getRequestOfShop(requestId, shopId);
        return save(requestId, ShopRequestCommentAuthor.ceo(ceoId), content);
    }

    public Long addCommentByAdmin(Long requestId, Long adminId, String content) {
        shopRequestIndexRecorder.getRequest(requestId);
        return save(requestId, ShopRequestCommentAuthor.admin(adminId), content);
    }

    private Long save(Long requestId, ShopRequestCommentAuthor author, String content) {
        ShopRequestComment saved = shopRequestCommentRepository.save(
            ShopRequestComment.of(requestId, author, content)
        );
        return saved.getId();
    }
}
