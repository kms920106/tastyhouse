package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.service.StorePriceVerificationItemSpec;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand;
import com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationRequestCommand;

@Service
@CeoApp
@Transactional
public class ShopStorePriceVerificationCommandService implements ShopStorePriceVerificationCommandUseCase {

    private final StorePriceVerificationService storePriceVerificationService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private static final TypeReference<List<ShopStorePriceVerificationItemCommand>> ITEMS_TYPE =
        new TypeReference<>() {
        };

    private final ObjectMapper objectMapper;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final StorePriceListImageSpecValidator storePriceListImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopStorePriceVerificationCommandService(
        ObjectMapper objectMapper,
        StorePriceVerificationService storePriceVerificationService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        ShopOwnershipValidator shopOwnershipValidator,
        StorePriceListImageSpecValidator storePriceListImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.objectMapper = objectMapper;
        this.storePriceVerificationService = storePriceVerificationService;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.storePriceListImageSpecValidator = storePriceListImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    @Override
    public Long requestVerification(ShopStorePriceVerificationRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<StorePriceVerificationItemSpec> specs = toItemSpecs(command.items());
        storePriceListImageSpecValidator.validate(file);

        Long priceListFileId = fileUploadCommandService.upload(file);

        ShopId targetShopId = ShopId.of(shopId);
        UploadedFileId targetPriceListFileId = UploadedFileId.of(priceListFileId);
        StorePriceVerification saved =
            storePriceVerificationService.request(targetShopId, targetPriceListFileId, specs, ceoId);

        shopRequestIndexRecorder.record(
            targetShopId,
            ShopRequestType.STORE_PRICE_VERIFICATION,
            saved.getId(),
            describeVerification(specs.size()),
            saved.getPriceListFileId(),
            ceoId
        );
        return saved.getId();
    }

    private List<StorePriceVerificationItemSpec> toItemSpecs(String items) {
        return parseItems(items).stream()
            .map(item -> StorePriceVerificationItemSpec.of(
                item.productId(),
                item.priceId(),
                item.storePrice(),
                Boolean.TRUE.equals(item.applyPickupSamePrice())
            ))
            .toList();
    }

    private List<ShopStorePriceVerificationItemCommand> parseItems(String items) {
        if (items == null || items.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }
        List<ShopStorePriceVerificationItemCommand> parsed;
        try {
            parsed = objectMapper.readValue(items, ITEMS_TYPE);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY,
                "인증 대상 목록(items)의 형식이 올바르지 않습니다.");
        }
        if (parsed == null || parsed.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }
        return parsed;
    }

    private String describeVerification(int itemCount) {
        return ShopRequestType.STORE_PRICE_VERIFICATION.getDescription() + " (메뉴 " + itemCount + "건)";
    }
}
