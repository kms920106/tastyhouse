package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface StorePriceVerificationLoadPort {

    Optional<StorePriceVerification> findById(StorePriceVerificationId id);

    boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses);

    List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId);
}
