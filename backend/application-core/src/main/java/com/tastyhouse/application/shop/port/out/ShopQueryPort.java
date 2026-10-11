package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopQueryPort {

    Optional<ShopVisibleDetailResult> findVisibleDetailById(Long shopId);

    boolean existsBookmark(Long shopId, Long memberId);
}
