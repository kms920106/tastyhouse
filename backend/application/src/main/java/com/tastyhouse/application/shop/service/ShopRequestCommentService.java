package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthor;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentPersistencePort;

@Service
public class ShopRequestCommentService {

    private final ShopRequestCommentPersistencePort shopRequestCommentPersistencePort;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCommentService(
        ShopRequestCommentPersistencePort shopRequestCommentPersistencePort,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopRequestCommentPersistencePort = shopRequestCommentPersistencePort;
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
        ShopRequestComment saved = shopRequestCommentPersistencePort.save(
            ShopRequestComment.of(requestId, author, content)
        );
        return saved.getId();
    }
}
