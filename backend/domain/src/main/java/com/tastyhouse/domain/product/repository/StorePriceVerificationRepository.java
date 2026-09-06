package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface StorePriceVerificationRepository {
    StorePriceVerification save(StorePriceVerification verification);

    Optional<StorePriceVerification> findById(StorePriceVerificationId id);

    Optional<StorePriceVerification> findLatestByShopId(ShopId shopId);

    boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses);

    void saveItem(StorePriceVerificationItem item);

    List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId);
}
