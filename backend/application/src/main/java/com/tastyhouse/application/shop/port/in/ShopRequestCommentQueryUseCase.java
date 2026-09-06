package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;

@AdminApp
public interface ShopRequestCommentQueryUseCase {

    List<ShopRequestCommentResult> getComments(Long requestId);
}
