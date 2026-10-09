package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationOwnerLatestResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationOwnerQueryPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.service.StorePriceVerificationService;
import com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

@Service
@Transactional(readOnly = true)
class ShopStorePriceVerificationQueryService implements ShopStorePriceVerificationQueryUseCase {

    private final StorePriceVerificationService storePriceVerificationService;
    private final StorePriceVerificationPort storePriceVerificationPort;
    private final StorePriceVerificationOwnerQueryPort storePriceVerificationOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStorePriceVerificationQueryService(
        StorePriceVerificationService storePriceVerificationService,
        StorePriceVerificationPort storePriceVerificationPort,
        StorePriceVerificationOwnerQueryPort storePriceVerificationOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.storePriceVerificationService = storePriceVerificationService;
        this.storePriceVerificationPort = storePriceVerificationPort;
        this.storePriceVerificationOwnerQueryPort = storePriceVerificationOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopStorePriceVerificationViewResult getLatestVerification(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        StorePriceVerificationOwnerLatestResult latest =
            storePriceVerificationOwnerQueryPort.findLatestByShopId(shopId).orElse(null);
        List<ShopStorePriceVerificationViewResult.UnverifiedItem> unverifiedItems =
            storePriceVerificationService.findUnverifiedItems(ShopId.of(shopId)).stream()
                .map(item -> new ShopStorePriceVerificationViewResult.UnverifiedItem(
                    item.productId(),
                    item.productName(),
                    item.reason().name()
                ))
                .toList();

        return new ShopStorePriceVerificationViewResult(
            latest == null ? null : latest.id(),
            latest == null ? null : latest.status(),
            storePriceVerificationPort.isStorePriceVerified(shopId),
            latest == null ? null : latest.rejectReason(),
            unverifiedItems
        );
    }

}
