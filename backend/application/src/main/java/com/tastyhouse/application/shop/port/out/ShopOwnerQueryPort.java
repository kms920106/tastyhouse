package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopOwnerQueryPort {

    List<ShopSuspensionResult> findSuspensions(Long shopId);

    List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId);
}
