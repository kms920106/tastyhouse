package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;

public interface ShopRequestCommentQueryUseCase {

    List<ShopRequestCommentResult> getComments(Long requestId);
}
