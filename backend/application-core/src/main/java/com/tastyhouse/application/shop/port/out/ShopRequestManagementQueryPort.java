package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

public interface ShopRequestManagementQueryPort {

    Optional<ShopRequestDetailResult> findRequestDetail(Long requestId);

    List<ShopRequestCommentResult> findComments(Long requestId);
}
