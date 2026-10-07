package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;

public interface ShopRequestCommentListQueryUseCase {

    List<ShopRequestCommentResult> getComments(Long ceoId, Long shopId, Long requestId);
}
