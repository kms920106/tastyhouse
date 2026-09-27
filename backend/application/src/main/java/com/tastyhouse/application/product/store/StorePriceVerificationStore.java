package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationStatePort;

public class StorePriceVerificationStore implements StorePriceVerificationRepository {
    private final StorePriceVerificationStatePort storePriceVerificationStatePort;

    public StorePriceVerificationStore(StorePriceVerificationStatePort storePriceVerificationStatePort) {
        this.storePriceVerificationStatePort = storePriceVerificationStatePort;
    }

    @Override
    public StorePriceVerification save(StorePriceVerification verification) {
        return StorePriceVerificationStateMapper.toDomain(
            storePriceVerificationStatePort.save(StorePriceVerificationStateMapper.toState(verification)));
    }

    @Override
    public Optional<StorePriceVerification> findById(StorePriceVerificationId id) {
        return storePriceVerificationStatePort.findById(id.value())
            .map(StorePriceVerificationStateMapper::toDomain);
    }

    @Override
    public Optional<StorePriceVerification> findLatestByShopId(ShopId shopId) {
        return storePriceVerificationStatePort.findLatestByShopId(shopId.value())
            .map(StorePriceVerificationStateMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return false;
        }
        return storePriceVerificationStatePort.existsByShopIdAndStatusIn(
            shopId.value(), statuses.stream().map(StorePriceVerificationStatus::name).toList());
    }

    @Override
    public void saveItem(StorePriceVerificationItem item) {
        storePriceVerificationStatePort.saveItem(StorePriceVerificationStateMapper.toState(item));
    }

    @Override
    public List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId) {
        return storePriceVerificationStatePort.findAllItemsByVerificationId(verificationId.value()).stream()
            .map(StorePriceVerificationStateMapper::toDomain)
            .toList();
    }
}
