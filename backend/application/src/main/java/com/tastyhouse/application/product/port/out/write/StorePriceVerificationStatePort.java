package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface StorePriceVerificationStatePort {
    StorePriceVerificationState save(StorePriceVerificationState verification);

    Optional<StorePriceVerificationState> findById(Long id);

    Optional<StorePriceVerificationState> findLatestByShopId(Long shopId);

    boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses);

    void saveItem(StorePriceVerificationItemState item);

    List<StorePriceVerificationItemState> findAllItemsByVerificationId(Long verificationId);
}
