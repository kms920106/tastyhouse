package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationQueryUseCase;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopStorePriceVerificationQueryService implements ShopStorePriceVerificationQueryUseCase {

    private final StorePriceVerificationService storePriceVerificationService;
    private final StorePriceVerificationOwnerReader storePriceVerificationReader;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStorePriceVerificationQueryService(
        StorePriceVerificationService storePriceVerificationService,
        StorePriceVerificationOwnerReader storePriceVerificationReader,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.storePriceVerificationService = storePriceVerificationService;
        this.storePriceVerificationReader = storePriceVerificationReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopStorePriceVerificationViewResult getLatestVerification(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        StorePriceVerification latest = storePriceVerificationReader.readLatest(shopId).orElse(null);
        List<ShopStorePriceVerificationViewResult.UnverifiedItem> unverifiedItems =
            storePriceVerificationService.findUnverifiedItems(ShopId.of(shopId)).stream()
                .map(item -> new ShopStorePriceVerificationViewResult.UnverifiedItem(
                    item.productId(),
                    item.productName(),
                    item.reason()
                ))
                .toList();

        return new ShopStorePriceVerificationViewResult(
            latest == null ? null : latest.getId(),
            latest == null ? null : latest.getStatus().name(),
            storePriceVerificationReader.readVerified(shopId),
            latest == null ? null : latest.getRejectReason(),
            unverifiedItems
        );
    }

}
